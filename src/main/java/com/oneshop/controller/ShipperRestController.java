package com.oneshop.controller;

import com.oneshop.entity.Order;
import com.oneshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipper")
@RequiredArgsConstructor
public class ShipperRestController {

    private final OrderService orderService;

    // Lấy danh sách đơn được giao cho shipper hiện tại
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername()); // hoặc lấy userId từ UserDetails principal mapping
        List<Order> orders = orderService.getOrdersByShipperUserId(userId);
        return ResponseEntity.ok(orders);
    }

    // Xem chi tiết 1 đơn (phải được gán)
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Order> getOrder(@AuthenticationPrincipal UserDetails userDetails,
                                          @PathVariable Long orderId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Order order = orderService.getOrderByIdForShipper(orderId, userId);
        return ResponseEntity.ok(order);
    }

    // Cập nhật trạng thái (ví dụ: DELIVERED / FAILED). body chứa note tùy chọn.
    @PostMapping("/orders/{orderId}/status")
    public ResponseEntity<Void> updateStatus(@AuthenticationPrincipal UserDetails userDetails,
                                             @PathVariable Long orderId,
                                             @RequestParam String status,
                                             @RequestParam(required = false) String note) {
        Long userId = Long.parseLong(userDetails.getUsername());
        orderService.updateOrderStatusHistoryByShipper(orderId, userId, status, note);
        return ResponseEntity.ok().build();
    }
}
