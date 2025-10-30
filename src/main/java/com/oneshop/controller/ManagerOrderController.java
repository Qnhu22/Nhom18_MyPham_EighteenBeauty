package com.oneshop.controller;

import java.time.LocalDate;
//import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import com.oneshop.entity.User;
import com.oneshop.entity.Order;
import com.oneshop.entity.OrderItem;
import com.oneshop.enums.OrderStatus;
import com.oneshop.repository.UserRepository;
import com.oneshop.service.OrderService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/manager/orders")
@RequiredArgsConstructor
public class ManagerOrderController {

	private final OrderService orderService;
    private final UserRepository userRepository;

    // -------------------- Danh sách đơn hàng --------------------
    @GetMapping
    public String listOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<Order> orders = orderService.filterOrders(status, keyword, date, start, end, page);

        model.addAttribute("orders", orders.getContent());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("date", date);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAttribute("shippers", userRepository.findByRoles("ROLE_SHIPPER"));
        model.addAttribute("allStatuses", OrderStatus.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());

        return "manager/orders/order-list";
    }

    // -------------------- Lấy chi tiết đơn hàng (trả JSON để hiển thị Modal) --------------------
    @GetMapping("/detail/{id}")
    @ResponseBody
    public ResponseEntity<?> getOrderDetail(@PathVariable Long id) {
        try {
            Order order = orderService.findById(id);

            Map<String, Object> data = new HashMap<>();
            data.put("orderId", order.getOrderId());
            data.put("customer", order.getUser() != null ? order.getUser().getFullName() : "Khách lẻ");
            data.put("email", order.getUser() != null ? order.getUser().getEmail() : "");
            data.put("status", order.getStatus().name());
            data.put("total", order.getTotalAmount());
            data.put("orderDate", order.getOrderDate());
            data.put("address", order.getAddress() != null ? order.getAddress().getFullAddress(): "—");
            data.put("shipper", order.getShipper() != null ? order.getShipper().getUser().getFullName() : "Chưa phân công");

            List<Map<String, Object>> itemList = new ArrayList<>();
            for (OrderItem item : order.getOrderItems()) {
                Map<String, Object> map = new HashMap<>();
                map.put("variantName", item.getProductVariant() != null ? item.getProductVariant().getName() : null);
                map.put("quantity", item.getQuantity());
                map.put("price", item.getPrice());
                itemList.add(map);
            }
            data.put("items", itemList);

            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // -------------------- Phân công shipper (AJAX) --------------------
    @PostMapping("/assign-shipper/{id}")
    @ResponseBody
    public ResponseEntity<?> assignShipper(@PathVariable Long id, @RequestParam Long shipperUserId) {
        try {
            orderService.assignShipper(id, shipperUserId);
            return ResponseEntity.ok("Phân công shipper thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // -------------------- Cập nhật trạng thái (AJAX) --------------------
    @PostMapping("/update-status/{id}")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        try {
            orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok("Cập nhật trạng thái thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // -------------------- Xóa đơn hàng --------------------
    @PostMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.deleteOrder(id);
            redirectAttributes.addFlashAttribute("success", "Xóa đơn hàng thành công!");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/manager/orders";
    }
}
