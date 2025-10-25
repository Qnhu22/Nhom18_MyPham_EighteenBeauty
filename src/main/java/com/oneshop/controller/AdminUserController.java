package com.oneshop.controller;

import com.oneshop.entity.Role;
import com.oneshop.entity.User;
import com.oneshop.repository.RoleRepository;
import com.oneshop.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserController(UserRepository userRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 📌 Danh sách + tìm kiếm
    @GetMapping
    public String listUsers(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<User> users;
        if (keyword != null && !keyword.isEmpty()) {
            users = userRepository.findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(keyword, keyword);
            model.addAttribute("keyword", keyword);
        } else {
            users = userRepository.findAll();
        }
        model.addAttribute("users", users);
        model.addAttribute("pageTitle", "Quản lý User");
        return "admin/user-list";
    }

    // 📌 Hiển thị form tạo user
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("pageTitle", "Tạo User");
        return "admin/user-create";
    }

    // 📌 Xử lý tạo user (Admin thêm -> active=true, không OTP)
    @PostMapping("/create")
    public String createUser(@ModelAttribute User user,
                             @RequestParam("roleId") Long roleId,
                             RedirectAttributes redirectAttributes) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role không tồn tại"));

        // Mã hóa mật khẩu
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Admin thêm user thì cho active luôn
        user.setActive(true);
        // Gắn role
        user.setRoles(new HashSet<>(List.of(role)));

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "✅ Tạo user mới thành công!");
        return "redirect:/admin/users";
    }

    // 📌 Hiển thị form update user
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        model.addAttribute("user", user);
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("pageTitle", "Cập nhật User");
        return "admin/user-edit";
    }

    // 📌 Xử lý update user
    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute User updatedUser,
                             @RequestParam("roleId") Long roleId,
                             RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role không tồn tại"));

        user.setFullName(updatedUser.getFullName());
        user.setEmail(updatedUser.getEmail());
        user.setUsername(updatedUser.getUsername());
        user.setPhone(updatedUser.getPhone());
        user.setActive(updatedUser.isActive());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        user.setRoles(new HashSet<>(List.of(role)));

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "✏️ Cập nhật user thành công!");
        return "redirect:/admin/users";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "🗑️ Xóa user thành công!");
        return "redirect:/admin/users";
    }
}
