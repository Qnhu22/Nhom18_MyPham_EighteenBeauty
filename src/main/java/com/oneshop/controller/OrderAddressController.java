package com.oneshop.controller;

import com.oneshop.dto.OrderAddressDTO;
import com.oneshop.entity.OrderAddress;
import com.oneshop.entity.User;
import com.oneshop.repository.UserRepository;
import com.oneshop.service.OrderAddressService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account/addresses")
@RequiredArgsConstructor
public class OrderAddressController {

    private final OrderAddressService addressService;
    private final UserRepository userRepository;

    /** 🏠 Hiển thị danh sách địa chỉ giao hàng */
    @GetMapping
    public String showAddresses(Authentication auth, Model model) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        // 🔹 Lấy thông tin user hiện tại
        String usernameOrEmail = auth.getName();
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản đăng nhập!"));

        // 🔹 Nạp danh sách địa chỉ
        model.addAttribute("addresses", addressService.getAddressesByUser(user));
        return "account/addresses";
    }

    /** 💾 Thêm hoặc cập nhật địa chỉ */
    @PostMapping("/save")
    public String saveAddress(@ModelAttribute OrderAddress address,
                              Authentication auth,
                              RedirectAttributes redirectAttrs) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String usernameOrEmail = auth.getName();
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản!"));

        boolean isUpdate = (address.getAddressId() != null);

        addressService.saveAddress(address, user);

        if (isUpdate)
            redirectAttrs.addFlashAttribute("msgSuccess", "✅ Cập nhật địa chỉ thành công!");
        else
            redirectAttrs.addFlashAttribute("msgSuccess", "✅ Thêm địa chỉ mới thành công!");

        return "redirect:/account/addresses";
    }

    /** 🗑️ Xóa địa chỉ */
    @PostMapping("/delete/{id}")
    public String deleteAddress(@PathVariable Long id,
                                Authentication auth,
                                RedirectAttributes redirectAttrs) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String usernameOrEmail = auth.getName();
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản!"));

        try {
            addressService.deleteAddress(id, user);
            redirectAttrs.addFlashAttribute("msgSuccess", "🗑️ Đã xóa địa chỉ thành công!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("msgError", "❌ Không thể xóa địa chỉ!");
        }

        return "redirect:/account/addresses";
    }

    /** ⭐ Đặt làm mặc định */
    @PostMapping("/set-default/{id}")
    public String setDefault(@PathVariable Long id,
                             Authentication auth,
                             RedirectAttributes redirectAttrs) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String usernameOrEmail = auth.getName();
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản!"));

        addressService.setDefaultAddress(id, user);
        redirectAttrs.addFlashAttribute("msgSuccess", "⭐ Đã đặt làm địa chỉ mặc định!");
        return "redirect:/account/addresses";
    }

    @ResponseBody
    @GetMapping("/{id}")
    public ResponseEntity<?> getAddressById(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "not_logged_in"));
        }

        OrderAddress address = addressService.getAddressById(id);
        if (address == null || !address.getUser().getUserId().equals(user.getUserId())) {
            return ResponseEntity.status(404).body(Map.of("error", "not_found"));
        }

        return ResponseEntity.ok(OrderAddressDTO.fromEntity(address));
    }

}
