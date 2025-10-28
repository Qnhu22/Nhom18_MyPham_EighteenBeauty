package com.oneshop.controller;

import com.oneshop.entity.*;
import com.oneshop.repository.ProductVariantRepository;
import com.oneshop.repository.UserRepository;
import com.oneshop.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;

    // 🛒 Hiển thị giỏ hàng
    @GetMapping
    public String showCart(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user != null) {
            Cart cart = cartService.getOrCreateCart(user);
            model.addAttribute("items", cart.getItems());
            model.addAttribute("total", cart.getTotalPrice());
        } else {
            List<Map<String, Object>> guestCart =
                    (List<Map<String, Object>>) session.getAttribute("guestCart");
            if (guestCart == null) guestCart = new ArrayList<>();

            model.addAttribute("items", guestCart);
            double total = guestCart.stream()
                    .mapToDouble(i -> (Double) i.get("subtotal"))
                    .sum();
            model.addAttribute("total", total);
        }

        return "cart";
    }

    // ➕ Thêm sản phẩm (AJAX)
    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addToCartAjax(@RequestParam Long variantId,
                                             @RequestParam(defaultValue = "1") int quantity,
                                             HttpSession session) {
        User user = (User) session.getAttribute("user");
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm!"));

        // ✅ Xử lý giỏ hàng cho khách (guest)
        List<Map<String, Object>> guestCart =
                (List<Map<String, Object>>) session.getAttribute("guestCart");
        if (guestCart == null) guestCart = new ArrayList<>();

        if (user != null) {
            // 🧾 Người dùng đăng nhập
            cartService.addToCart(user, variantId, quantity);
        } else {
            // 🧾 Người dùng chưa đăng nhập → lưu trong session
            Optional<Map<String, Object>> exist = guestCart.stream()
                    .filter(i -> i.get("variantId").equals(variantId))
                    .findFirst();

            if (exist.isPresent()) {
                // 🔄 Nếu đã có cùng variantId thì cộng dồn số lượng
                Map<String, Object> item = exist.get();
                int newQty = (int) item.get("quantity") + quantity;
                double price = (double) item.get("price");
                item.put("quantity", newQty);
                item.put("subtotal", price * newQty);
            } else {
                // 🆕 Thêm mới
                Map<String, Object> newItem = new HashMap<>();

                // 🧾 Lưu tên hiển thị chi tiết để tránh gộp các biến thể cùng sản phẩm
                String displayName = variant.getProduct().getName();
                if (variant.getColor() != null || variant.getSize() != null) {
                    displayName += " - ";
                    if (variant.getColor() != null) displayName += variant.getColor();
                    if (variant.getSize() != null) displayName += " / " + variant.getSize();
                }

                newItem.put("productId", variant.getProduct().getProductId());
                newItem.put("variantId", variantId);
                newItem.put("name", displayName); // ✅ tên chứa màu/size
                newItem.put("image", variant.getImageUrl());
                newItem.put("price", variant.getPrice().doubleValue());
                newItem.put("quantity", quantity);
                newItem.put("subtotal", variant.getPrice().doubleValue() * quantity);
                newItem.put("color", variant.getColor());
                newItem.put("size", variant.getSize());
                guestCart.add(newItem);
            }

            // ✅ Lưu lại session sau mỗi thay đổi
            session.setAttribute("guestCart", guestCart);
        }

        // ✅ Trả về JSON để JS cập nhật ngay
        Map<String, Object> resp = new HashMap<>();
        if (user != null) {
            resp.put("cartCount", cartService.countItems(user));
            resp.put("total", cartService.calculateTotal(user));
        } else {
            double total = guestCart.stream().mapToDouble(i -> (Double) i.get("subtotal")).sum();

            // ✅ FIX QUAN TRỌNG: đếm tổng quantity, không phải số item
            int totalQty = guestCart.stream().mapToInt(i -> (int) i.get("quantity")).sum();

            resp.put("cartCount", totalQty);
            resp.put("total", total);
        }
        return resp;
    }

    // 🗑 Xóa item
    @PostMapping("/remove")
    public String removeItem(@RequestParam Long variantId, HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user != null) {
            cartService.removeItem(user, variantId);
        } else {
            List<Map<String, Object>> guestCart =
                    (List<Map<String, Object>>) session.getAttribute("guestCart");
            if (guestCart != null) {
                guestCart.removeIf(item -> item.get("variantId").equals(variantId));
                session.setAttribute("guestCart", guestCart);
            }
        }

        return "redirect:/cart";
    }

    // 🧹 Xóa toàn bộ giỏ
    @PostMapping("/clear")
    public String clearCart(HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user != null) {
            cartService.clearCart(user);
        } else {
            session.removeAttribute("guestCart");
        }

        return "redirect:/cart";
    }

    // 💳 Thanh toán (bắt buộc đăng nhập)
    @GetMapping("/checkout")
    public String checkout(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?checkout=true";
        }
        return "checkout";
    }

    // 📦 API kiểm tra trạng thái giỏ (cho header mini-cart)
    @GetMapping("/api/status")
    @ResponseBody
    public Map<String, Object> getCartStatus(HttpSession session) {
        User user = (User) session.getAttribute("user");
        Map<String, Object> resp = new HashMap<>();

        if (user != null) {
            resp.put("cartCount", cartService.countItems(user));
            resp.put("total", cartService.calculateTotal(user));
        } else {
            List<Map<String, Object>> guestCart =
                    (List<Map<String, Object>>) session.getAttribute("guestCart");
            if (guestCart == null) guestCart = new ArrayList<>();

            double total = guestCart.stream().mapToDouble(i -> (Double) i.get("subtotal")).sum();
            int totalQty = guestCart.stream().mapToInt(i -> (int) i.get("quantity")).sum();

            resp.put("cartCount", totalQty);
            resp.put("total", total);
        }
        return resp;
    }
}
