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
    public String checkout(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login?checkout=true";
        }

        // ✅ Dùng fetch join để load luôn defaultAddress & addresses
        User user = userRepository.findById(sessionUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        // 🧾 Lấy giỏ hàng
        Cart cart = cartService.getOrCreateCart(user);
        model.addAttribute("items", cart.getItems());
        model.addAttribute("total", cart.getTotalPrice());

        // 🏠 Xác định địa chỉ giao hàng mặc định
        OrderAddress defaultAddress = user.getDefaultAddress();

        if (defaultAddress == null) {
            // Nếu user chưa có defaultAddressId -> tìm dòng có isDefault = 1
            for (OrderAddress addr : user.getAddresses()) {
                if (addr.isDefaultAddress()) {
                    defaultAddress = addr;
                    break;
                }
            }

            // Nếu vẫn không có, lấy địa chỉ đầu tiên
            if (defaultAddress == null && !user.getAddresses().isEmpty()) {
                defaultAddress = user.getAddresses().iterator().next();
            }
        }

        model.addAttribute("defaultAddress", defaultAddress);

        return "account/checkout";
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
    
 // 🔄 Cập nhật số lượng sản phẩm trong giỏ
    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateQuantity(@RequestParam Long variantId,
                                              @RequestParam int quantity,
                                              HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        User user = (User) session.getAttribute("user");

        if (user != null) {
            // ✅ Nếu là user đã đăng nhập
            cartService.updateItem(user, variantId, quantity);

            double itemSubtotal = cartService.getItemSubtotal(user, variantId);
            double total = cartService.calculateTotal(user);

            resp.put("itemSubtotal", itemSubtotal);
            resp.put("total", total);
        } else {
            // ✅ Nếu là khách (guest)
            List<Map<String, Object>> guestCart =
                    (List<Map<String, Object>>) session.getAttribute("guestCart");
            if (guestCart == null) guestCart = new ArrayList<>();

            guestCart.forEach(item -> {
                if (item.get("variantId").equals(variantId)) {
                    item.put("quantity", quantity);
                    double price = (double) item.get("price");
                    item.put("subtotal", price * quantity);
                }
            });

            session.setAttribute("guestCart", guestCart);

            double total = guestCart.stream().mapToDouble(i -> (Double) i.get("subtotal")).sum();
            double itemSubtotal = guestCart.stream()
                    .filter(i -> i.get("variantId").equals(variantId))
                    .mapToDouble(i -> (Double) i.get("subtotal"))
                    .findFirst().orElse(0.0);

            resp.put("itemSubtotal", itemSubtotal);
            resp.put("total", total);
        }

        return resp;
    }

}
