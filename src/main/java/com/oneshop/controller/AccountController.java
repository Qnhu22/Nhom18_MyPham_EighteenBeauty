package com.oneshop.controller;

import com.oneshop.entity.Shipper;
import com.oneshop.entity.User;
import com.oneshop.repository.ShipperRepository;
import com.oneshop.repository.UserRepository;
import com.oneshop.service.ShipperService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ShipperRepository shipperRepository;
    private final ShipperService shipperService; 

    // ===================== TRANG THÔNG TIN TÀI KHOẢN =====================
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/account")
    public String accountInfo(Authentication auth, Model model) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String usernameOrEmail = auth.getName();

        User user = userRepository.findByUsernameFetchRoles(usernameOrEmail)
                .or(() -> userRepository.findByEmailFetchRoles(usernameOrEmail))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));

        List<String> roles = user.getRoles().stream()
                .map(r -> prettifyRole(r.getRoleName()))
                .toList();
     
     // Nếu là shipper → lấy thêm thông tin từ bảng shipper
        if (roles.contains("Nhân viên giao hàng")) {
            Shipper shipper = shipperService.getShipperById(user.getUserId());
            model.addAttribute("shipper", shipper);
        }
        
        model.addAttribute("u", user);
        model.addAttribute("roles", roles);
        model.addAttribute("maskedPassword", "********");

        return "account/info";
    }

    private String prettifyRole(String role) {
        return switch (role) {
            case "ROLE_ADMIN" -> "Quản trị viên";
            case "ROLE_MANAGER" -> "Quản lý cửa hàng";
            case "ROLE_SHIPPER" -> "Nhân viên giao hàng";
            case "ROLE_USER" -> "Khách hàng";
            default -> role;
        };
    }

    // ===================== CẬP NHẬT HỒ SƠ NGƯỜI DÙNG =====================
    @PostMapping("/account/update-profile")
    public String updateProfile(
            @RequestParam(required = false) MultipartFile avatarFile,
            @RequestParam String fullName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime birthDate,
            @RequestParam(required = false) String area,      // ✅ thêm cho shipper
            @RequestParam(required = false) String status,    // ✅ thêm cho shipper
            Authentication auth) throws IOException {

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        user.setFullName(fullName);
        user.setPhone(phone);
        user.setGender(gender);
        user.setBirthDate(birthDate);

        // 🔹 Nếu có chọn ảnh mới
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // 🧹 Xóa ảnh cũ nếu có
            if (user.getAvatar() != null && user.getAvatar().startsWith("/images/avatar/")) {
                Path oldPath = Paths.get("src/main/resources/static" + user.getAvatar());
                Files.deleteIfExists(oldPath);
            }

            // 📸 Lưu ảnh mới vào static/images/avatar/
            String uploadDir = "src/main/resources/static/images/avatar/";
            Files.createDirectories(Paths.get(uploadDir));

            // 🔹 Đặt tên file mới ngẫu nhiên (UUID để tránh trùng cache)
            String extension = avatarFile.getOriginalFilename()
                    .substring(avatarFile.getOriginalFilename().lastIndexOf('.') + 1);
            String filename = "avatar_" + user.getUserId() + "_" + UUID.randomUUID() + "." + extension;

            Path path = Paths.get(uploadDir + filename);
            Files.write(path, avatarFile.getBytes());

            user.setAvatar("/images/avatar/" + filename);
        }

        userRepository.save(user);
        
     

   
        return "redirect:/account";
    }
    
    @PostMapping("/account/update-shipper")
    @PreAuthorize("isAuthenticated()")
    public String updateShipperInfo(
            @RequestParam String area,
            @RequestParam String status,
            Authentication auth
    ) {
        // Lấy thông tin user hiện tại
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        // Tìm shipper tương ứng
        Shipper shipper = shipperService.getShipperById(user.getUserId());
        if (shipper == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin shipper");
        }

        // Cập nhật khu vực và trạng thái
        shipper.setArea(area);
        shipper.setStatus(status);

        // Lưu lại
        shipperService.saveShipper(shipper);

        return "redirect:/account";
    }

    
 // ===================== ĐỔI MẬT KHẨU =====================
    @PostMapping("/account/change-password")
    @PreAuthorize("isAuthenticated()")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication auth,
            Model model) {

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

        String messageError = null;
        String messageSuccess = null;

        if (!encoder.matches(currentPassword, user.getPassword())) {
            messageError = "❌ Mật khẩu hiện tại không đúng!";
        } else if (!newPassword.equals(confirmPassword)) {
            messageError = "⚠️ Mật khẩu mới và xác nhận không khớp!";
        } else if (encoder.matches(newPassword, user.getPassword())) {
            messageError = "⚠️ Mật khẩu mới không được trùng mật khẩu cũ!";
        } else {
            user.setPassword(encoder.encode(newPassword));
            userRepository.save(user);
            messageSuccess = "✅ Đổi mật khẩu thành công!";
        }

        // 🔹 Nạp lại dữ liệu user & roles (như trong accountInfo)
        List<String> roles = user.getRoles().stream()
                .map(r -> switch (r.getRoleName()) {
                    case "ROLE_ADMIN" -> "Quản trị viên";
                    case "ROLE_MANAGER" -> "Quản lý cửa hàng";
                    case "ROLE_SHIPPER" -> "Nhân viên giao hàng";
                    case "ROLE_USER" -> "Khách hàng";
                    default -> r.getRoleName();
                })
                .toList();

        model.addAttribute("u", user);
        model.addAttribute("roles", roles);
        model.addAttribute("maskedPassword", "********");

        // 🔹 Thêm thông báo & tự mở modal lại
        model.addAttribute("changePassError", messageError);
        model.addAttribute("changePassSuccess", messageSuccess);
        model.addAttribute("openChangePassModal", true);

        return "account/info";
    }

}
