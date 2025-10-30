package com.oneshop.controller;

import com.oneshop.entity.Role;
import com.oneshop.entity.User;
import com.oneshop.repository.RoleRepository;
import com.oneshop.repository.UserRepository;
import com.oneshop.service.impl.UserServiceImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.HashSet;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserServiceImpl userService; // sử dụng service có OTP

    public AuthController(UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          UserServiceImpl userService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    // ====================== ĐĂNG KÝ ======================
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register"; // register.html
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam String fullName,
                                  @RequestParam String email,
                                  @RequestParam String username,
                                  @RequestParam String password,
                                  @RequestParam String phone,
                                  RedirectAttributes redirectAttributes) {
        if (userRepository.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "❌ Email đã tồn tại!");
            return "redirect:/register";
        }

        if (userRepository.findByUsername(username).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "❌ Username đã tồn tại!");
            return "redirect:/register";
        }

        // tạo user mới với active=false
        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .username(username)
                .phone(phone)
                .password(passwordEncoder.encode(password))
                .active(false) // 👈 chưa active
                .build();

        Role defaultRole = roleRepository.findByRoleName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER không tồn tại trong DB"));
        // ✅ dùng HashSet để tránh lỗi Hibernate merge
        user.setRoles(new HashSet<>(Collections.singleton(defaultRole)));

        // lưu user
        userRepository.save(user);

        // gửi OTP
        userService.sendOtp(user);

        // chuyển sang trang verify OTP
        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("success", "📩 Đăng ký thành công! Vui lòng kiểm tra email để nhập OTP.");
        return "redirect:/verify-otp";
    }

    // ====================== XÁC THỰC OTP ======================
    @GetMapping("/verify-otp")
    public String showVerifyOtpForm() {
        return "verify_otp"; // verify_otp.html
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email,
                            @RequestParam String otp,
                            RedirectAttributes redirectAttributes) {
        boolean valid = userService.verifyOtp(email, otp);
        if (valid) {
            redirectAttributes.addFlashAttribute("success", "✅ Xác thực thành công! Mời đăng nhập.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "❌ OTP không hợp lệ hoặc đã hết hạn.");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/verify-otp";
        }
    }

    // ====================== LOGIN ======================
    @GetMapping("/login")
    public String showLoginForm(@ModelAttribute("error") String error,
                                @ModelAttribute("success") String success) {
        if (error != null && !error.isEmpty()) {
            System.err.println("❌ [LOGIN ERROR] " + error);
        }
        if (success != null && !success.isEmpty()) {
            System.out.println("✅ [SUCCESS] " + success);
        }
        return "login"; // login.html
    }

    // ====================== QUÊN MẬT KHẨU ======================
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot_password"; // forgot_password.html
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email,
                                        RedirectAttributes redirectAttributes) {
        if (userRepository.findByEmail(email).isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "❌ Email không tồn tại trong hệ thống.");
            return "redirect:/forgot-password";
        }

        userService.forgotPassword(email);
        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("success", "📩 OTP đã được gửi đến email. Vui lòng nhập OTP để đổi mật khẩu.");
        return "redirect:/reset-password";
    }

    // ====================== RESET MẬT KHẨU ======================
    @GetMapping("/reset-password")
    public String showResetPasswordForm() {
        return "reset_password"; // reset_password.html
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String email,
                                       @RequestParam String otp,
                                       @RequestParam String newPassword,
                                       RedirectAttributes redirectAttributes) {
        boolean ok = userService.resetPassword(email, otp, newPassword);
        if (ok) {
            redirectAttributes.addFlashAttribute("success", "✅ Đặt lại mật khẩu thành công! Mời đăng nhập.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "❌ OTP không hợp lệ hoặc đã hết hạn.");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/reset-password";
        }
    }
}
