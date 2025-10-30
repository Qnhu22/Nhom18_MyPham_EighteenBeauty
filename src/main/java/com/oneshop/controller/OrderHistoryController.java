package com.oneshop.controller;

import com.oneshop.entity.Order;
import com.oneshop.entity.OrderAddress;
import com.oneshop.entity.User;
import com.oneshop.enums.OrderStatus;
import com.oneshop.repository.OrderAddressRepository;
import com.oneshop.repository.UserRepository;
import com.oneshop.security.UserPrincipal;
import com.oneshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller hiển thị lịch sử đơn hàng cho người dùng
 * và xử lý các hành động huỷ / trả hàng.
 */
@Controller
@RequiredArgsConstructor
public class OrderHistoryController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final OrderAddressRepository orderAddressRepository;

    /** 🧾 Lịch sử đơn hàng */
    @GetMapping("/account/orders")
    public String orderHistory(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        if (principal == null || principal.getUser() == null) {
            return "redirect:/login";
        }

        User user = userRepository.findById(principal.getUser().getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user trong DB!"));

        List<Order> orders = orderService.getOrdersByUser(user);

        Map<Long, OrderAddress> addressMap = new HashMap<>();
        for (Order order : orders) {
            OrderAddress address = (order.getAddress() != null)
                    ? orderAddressRepository.findById(order.getAddress().getAddressId()).orElse(null)
                    : orderAddressRepository.findFirstByUserAndIsDefaultTrue(user).orElse(null);
            if (address != null) addressMap.put(order.getOrderId(), address);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("addressMap", addressMap);
        return "account/order-history";
    }

    /**
     * 🔄 API cập nhật trạng thái (AJAX)
     * Cho phép user cập nhật: cancel / return
     * Shipper, Manager có thể gọi tương tự
     */
    @PostMapping("/account/orders/{orderId}/update-status")
    @ResponseBody
    public Map<String, Object> updateStatus(@PathVariable Long orderId,
                                            @AuthenticationPrincipal UserPrincipal principal,
                                            @RequestParam String action) {
        Map<String, Object> res = new HashMap<>();
        if (principal == null) {
            res.put("success", false);
            res.put("message", "Vui lòng đăng nhập lại!");
            return res;
        }

        User user = principal.getUser();
        boolean ok = false;
        String message = "";
        OrderStatus newStatus = null;

        switch (action.toLowerCase()) {
            case "cancel":
                ok = orderService.changeStatus(orderId, user, OrderStatus.CANCELLED, "Khách hủy đơn hàng.");
                newStatus = OrderStatus.CANCELLED;
                message = ok ? "Đã huỷ đơn hàng #" + orderId + " thành công!" : "Không thể huỷ đơn hàng này!";
                break;
            case "return":
                ok = orderService.changeStatus(orderId, user, OrderStatus.RETURNED, "Khách yêu cầu trả hàng/hoàn tiền.");
                newStatus = OrderStatus.RETURNED;
                message = ok ? "Đã gửi yêu cầu trả hàng/hoàn tiền cho đơn #" + orderId : "Không thể thực hiện yêu cầu này!";
                break;
            default:
                res.put("success", false);
                res.put("message", "Hành động không hợp lệ!");
                return res;
        }

        res.put("success", ok);
        res.put("newStatus", newStatus != null ? newStatus.name() : "");
        res.put("message", message);
        return res;
    }
}
