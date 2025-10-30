// package com.oneshop.controller;
//
// import com.oneshop.entity.User;
// import com.oneshop.service.OrderService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
//
// @Controller
// @RequiredArgsConstructor
// public class OrderController {
//
//     private final OrderService orderService;
//
//     @GetMapping("/account/orders")
//     public String viewOrderHistory(@AuthenticationPrincipal User user, Model model) {
//         model.addAttribute("orders", orderService.getOrdersByUser(user));
//         return "account/order-history";
//     }
// }
