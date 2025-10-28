package com.oneshop.config;

import com.oneshop.entity.User;
import com.oneshop.repository.UserRepository;
import com.oneshop.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalCartAdvice {

    private final CartService cartService;
    private final UserRepository userRepository;

    @ModelAttribute
    public void addCartAttributes(Model model, Authentication auth, HttpSession session) {
        int cartCount = 0;
        double total = 0;
        Object items = null;
        User user = null;

        // ✅ Nếu user đã đăng nhập
        if (auth != null && auth.isAuthenticated()) {
            user = userRepository.findByUsername(auth.getName()).orElse(null);
        }

        if (user != null) {
            items = cartService.getCartItems(user);
            cartCount = cartService.countItems(user);
            total = cartService.calculateTotal(user);
        } else {
            // ✅ Guest: lấy từ session
            List<Map<String, Object>> guestCart =
                    (List<Map<String, Object>>) session.getAttribute("guestCart");
            if (guestCart != null) {
                items = guestCart;
                cartCount = guestCart.size();
                total = guestCart.stream()
                        .mapToDouble(i -> (Double) i.get("subtotal"))
                        .sum();
            }
        }

        model.addAttribute("items", items);
        model.addAttribute("cartCount", cartCount);
        model.addAttribute("total", total);
    }
}
