package com.oneshop.security;

import com.oneshop.entity.User;
import com.oneshop.repository.UserRepository;
import com.oneshop.service.CartService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Transactional
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final CartService cartService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        HttpSession session = request.getSession(false);
        if (session == null) {
            session = request.getSession(true);
        }

        // 🧠 Lấy user đang đăng nhập
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        // ✅ Merge giỏ hàng guest vào DB
        if (user != null) {
            List<Map<String, Object>> guestCart =
                    (List<Map<String, Object>>) session.getAttribute("guestCart");

            if (guestCart != null && !guestCart.isEmpty()) {
                for (Map<String, Object> item : guestCart) {
                    Long variantId = ((Number) item.get("variantId")).longValue();
                    int quantity = (int) item.get("quantity");
                    cartService.addToCart(user, variantId, quantity);
                }
                session.removeAttribute("guestCart");
            }

            // Gắn user vào session để header nhận biết
            session.setAttribute("user", user);
        }

        // 🧭 Điều hướng theo role
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        if (roles.contains("ROLE_ADMIN")) {
            response.sendRedirect("/admin/dashboard");
        } else if (roles.contains("ROLE_MANAGER")) {
            response.sendRedirect("/manager/dashboard");
        } else if (roles.contains("ROLE_SHIPPER")) {
            response.sendRedirect("/shipper/dashboard");
        } else if (roles.contains("ROLE_USER")) {
            // ✅ Quay lại trang khách (home/shop) thay vì dashboard riêng
            response.sendRedirect("/");
        } else {
            response.sendRedirect("/");
        }
    }
}
