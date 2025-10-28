package com.oneshop.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.oneshop.dto.ChartData;
import com.oneshop.dto.PerformanceStats;
import com.oneshop.entity.Order;
import com.oneshop.entity.Shipper;
import com.oneshop.entity.User;
import com.oneshop.repository.UserRepository;
import com.oneshop.service.OrderService;
import com.oneshop.service.ReportService;
import com.oneshop.service.ShipperService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/shipper")
@RequiredArgsConstructor
public class ShipperController {

    private final OrderService orderService;
    private final ShipperService shipperService;
    private final UserRepository userRepository;
    private final ReportService reportService; 

    

    // 🧩 Lấy ID người dùng hiện tại (từ username hoặc từ entity)
    private Long getCurrentUserId(UserDetails principal) {
        try {
            return Long.parseLong(principal.getUsername());
        } catch (NumberFormatException ex) {
            User u = userRepository.findByUsername(principal.getUsername()).orElseThrow();
            return u.getUserId();
        }
    }

 // 🏠 Trang Dashboard Shipper
    @SuppressWarnings("deprecation")
	@GetMapping({"/dashboard", ""})
    public String home(@AuthenticationPrincipal UserDetails principal, Model model) throws JsonProcessingException {
        Long userId = getCurrentUserId(principal);
        Shipper shipper = shipperService.getShipperByUserId(userId);
    
        
        if (shipper == null) {
            model.addAttribute("error", "Không tìm thấy thông tin shipper!");
            return "error";
        }

        Long shipperId = shipper.getShipperId();

        long confirmedCount = orderService.getOrdersByShipperUserIdAndStatus(userId, "CONFIRMED").size();
        long shippingCount = orderService.getOrdersByShipperUserIdAndStatus(userId, "SHIPPING").size();
        long deliveredCount = orderService.getOrdersByShipperUserIdAndStatus(userId, "DELIVERED").size();
        long cancelledCount = orderService.getOrdersByShipperUserIdAndStatus(userId, "CANCELLED").size();
        long returnedCount = orderService.getOrdersByShipperUserIdAndStatus(userId, "RETURNED").size();
        long totalOrders = orderService.countTotalOrdersByShipper(shipperId);

        BigDecimal totalRevenue = orderService.calculateTotalRevenueByShipper(shipperId);

        List<ChartData> chartData = orderService.getMonthlyDeliveredStats(shipperId);
        PerformanceStats perfStats = orderService.getPerformanceStats(shipperId);

        // ✅ chuyển chartData sang JSON
        ObjectMapper mapper = new ObjectMapper();
        String chartDataJson = mapper.writeValueAsString(chartData);
        mapper.configure(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN, true);
        
        model.addAttribute("shipperName", shipper.getUser().getFullName());
        model.addAttribute("shipperId", shipperId);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("shippingCount", shippingCount);
        model.addAttribute("deliveredCount", deliveredCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("returnedCount", returnedCount);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalRevenue", totalRevenue);

        model.addAttribute("chartDataJson", chartDataJson); 
        model.addAttribute("perfStats", perfStats);

        return "dashboard/shipper-dashboard";
    }


    // 📦 Danh sách đơn hàng theo trạng thái
    @GetMapping("/orders")
    public String orders(@AuthenticationPrincipal UserDetails principal,
                         @RequestParam(required = false) String status,
                         Model model,
                         @ModelAttribute("successMessage") String successMessage) {
        Long userId = getCurrentUserId(principal);
        List<Order> orders;

        if (status == null || status.isBlank()) {
            orders = orderService.getOrdersByShipperUserId(userId);
        } else {
            orders = orderService.getOrdersByShipperUserIdAndStatus(userId, status);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("status", status);
        if (successMessage != null && !successMessage.isEmpty()) {
            model.addAttribute("successMessage", successMessage);
        }

        return "shipper/orders";
    }

    // 🔍 Chi tiết đơn hàng
    @GetMapping("/order/{orderId}")
    public String orderDetail(@AuthenticationPrincipal UserDetails principal,
                              @PathVariable Long orderId,
                              Model model) {
        Long userId = getCurrentUserId(principal);
        Order order = orderService.getOrderByIdForShipper(orderId, userId);
        model.addAttribute("order", order);
        return "shipper/order-detail";
    }

    // 🔄 Cập nhật trạng thái đơn hàng (CONFIRMED → SHIPPING → DELIVERED / RETURNED / CANCELLED)
    @PostMapping("/order/{orderId}/update")
    public String updateStatus(@AuthenticationPrincipal UserDetails principal,
                               @PathVariable Long orderId,
                               @RequestParam String status,
                               @RequestParam(required = false) String note,
                               RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId(principal);

        // Cập nhật trạng thái đơn hàng
        orderService.updateOrderStatusHistoryByShipper(orderId, userId, status, note);

        // Thêm thông báo khi quay lại danh sách
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công!");
        return "redirect:/shipper/orders";
    }

    

    // 📝 Cập nhật hồ sơ Shipper
    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails principal,
                                @ModelAttribute Shipper shipperForm,
                                RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId(principal);
        Shipper shipper = shipperService.getShipperByUserId(userId);
        if (shipper == null) return "redirect:/shipper/profile";

        shipper.setArea(shipperForm.getArea());
        shipper.setStatus(shipperForm.getStatus());
        shipper.setTotalFailed(shipperForm.getTotalFailed());
        shipper.setTotalDelivered(shipperForm.getTotalDelivered());
        shipperService.save(shipper);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
        return "redirect:/shipper/profile";
    }
    
    // 📊 Xuất file thống kê (Excel / PDF)
    @GetMapping("/statistics/export")
    public ResponseEntity<byte[]> exportReport(@AuthenticationPrincipal UserDetails principal) {
        Long userId = getCurrentUserId(principal);
        Shipper shipper = shipperService.getShipperByUserId(userId);

        byte[] fileData = reportService.generateRevenueReport(shipper.getShipperId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "report_shipper.xlsx");

        return ResponseEntity.ok().headers(headers).body(fileData);
    }

}
