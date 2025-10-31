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
        	Shipper shipper = shipperService.getShipperByUserId(user.getUserId());

            if (shipper != null) {
                model.addAttribute("shipper", shipper);
            } else {
                model.addAttribute("shipper", new Shipper()); // tránh null
            }
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
            @RequestParam(required = false) String birthDate, // nhận chuỗi
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String status,
            Authentication auth) throws IOException {

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        user.setFullName(fullName);
        user.setPhone(phone);
        user.setGender(gender);

        // ✅ Parse thủ công yyyy-MM-dd → LocalDateTime
        if (birthDate != null && !birthDate.isBlank()) {
            try {
                LocalDateTime parsed = LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
                user.setBirthDate(parsed);
            } catch (Exception e) {
                System.err.println("⚠️ Lỗi parse ngày sinh: " + e.getMessage());
            }
        }

        // 🔹 Nếu có chọn ảnh mới
        if (avatarFile != null && !avatarFile.isEmpty()) {
            if (user.getAvatar() != null && user.getAvatar().startsWith("/images/avatar/")) {
                Path oldPath = Paths.get("src/main/resources/static" + user.getAvatar());
                Files.deleteIfExists(oldPath);
            }

            String uploadDir = "src/main/resources/static/images/avatar/";
            Files.createDirectories(Paths.get(uploadDir));

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
     // Tìm shipper tương ứng
        Shipper shipper = shipperRepository.findByUser_UserId(user.getUserId())
        	     .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin shipper!"));


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
