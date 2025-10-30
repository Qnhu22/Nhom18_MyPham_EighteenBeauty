package com.oneshop.controller;

import com.oneshop.entity.Role;
import com.oneshop.entity.User;
import com.oneshop.entity.UserActionLog;
import com.oneshop.entity.OrderAddress;
import com.oneshop.repository.RoleRepository;
import com.oneshop.repository.UserRepository;
import com.oneshop.service.OrderAddressService;
import com.oneshop.service.ShipperService;
import com.oneshop.service.UserActionLogService;
import com.oneshop.service.UserService;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

	private final UserRepository userRepository;
	private final UserService userService;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final OrderAddressService addressService;
	private final UserActionLogService logService;
	private final ShipperService shipperService;

	public AdminUserController(UserRepository userRepository, UserService userService, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder, OrderAddressService addressService, UserActionLogService logService,
			ShipperService shipperService) {
		this.userRepository = userRepository;
		this.userService = userService;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.addressService = addressService;
		this.logService = logService;
		this.shipperService = shipperService;
	}

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

	@GetMapping("/create")
	public String showCreateForm(Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("roles", roleRepository.findAll());
		model.addAttribute("addresses", addressService.getAllAddresses());
		model.addAttribute("pageTitle", "Tạo User");
		return "admin/user-form";
	}

	@PostMapping("/save")
	public String createUser(@ModelAttribute User user, @RequestParam("roleId") Long roleId,
			@RequestParam(value = "defaultAddressId", required = false) Long defaultAddressId, HttpSession session,
			RedirectAttributes redirectAttributes) {

		Role role = roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role không tồn tại"));

		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setActive(true);
		user.setRoles(new HashSet<>(List.of(role)));

		if (defaultAddressId != null) {
			OrderAddress selected = addressService.getById(defaultAddressId);
			if (selected != null) {
				userService.setDefaultAddress(user, selected);
			}
		}

		userRepository.save(user);

		if (role.getRoleName().equals("ROLE_SHIPPER")) {
			shipperService.createIfAbsent(user);
		}

		User admin = (User) session.getAttribute("loggedInUser");
		if (admin != null) {
			UserActionLog log = UserActionLog.builder().admin(admin).targetUser(user).actionType("Tạo mới")
					.description("Admin tạo user: " + user.getUsername()).actionTime(LocalDateTime.now()).build();
			logService.save(log);
		}

		redirectAttributes.addFlashAttribute("success", "✅ Tạo user mới thành công!");
		return "redirect:/admin/users";
	}

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model) {
		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User không tồn tại"));

		model.addAttribute("user", user);
		model.addAttribute("roles", roleRepository.findAll());
		model.addAttribute("addresses", addressService.getAllAddresses());
		model.addAttribute("pageTitle", "Cập nhật User");
		return "admin/user-edit";
	}

	@PostMapping("/update/{id}")
	public String updateUser(@PathVariable Long id, @ModelAttribute User updatedUser,
			@RequestParam("roleId") Long roleId,
			@RequestParam(value = "defaultAddressId", required = false) Long defaultAddressId, HttpSession session,
			RedirectAttributes redirectAttributes) {
		User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User không tồn tại"));

		Role role = roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role không tồn tại"));

		user.setFullName(updatedUser.getFullName());
		user.setEmail(updatedUser.getEmail());
		user.setUsername(updatedUser.getUsername());
		user.setPhone(updatedUser.getPhone());
		user.setActive(updatedUser.isActive());

		if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
			user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
		}

		user.setRoles(new HashSet<>(List.of(role)));

		if (defaultAddressId != null) {
			OrderAddress selected = addressService.getById(defaultAddressId);
			if (selected != null) {
				userService.setDefaultAddress(user, selected);
			}
		}

		userRepository.save(user);

		if (role.getRoleName().equals("ROLE_SHIPPER")) {
			shipperService.createIfAbsent(user);
		}

		User admin = (User) session.getAttribute("loggedInUser");
		if (admin != null) {
			UserActionLog log = UserActionLog.builder().admin(admin).targetUser(user).actionType("Chỉnh sửa")
					.description("Admin cập nhật user: " + user.getUsername()).actionTime(LocalDateTime.now()).build();
			logService.save(log);
		}

		redirectAttributes.addFlashAttribute("success", "✏️ Cập nhật user thành công!");
		return "redirect:/admin/users";
	}

	@GetMapping("/delete/{id}")
	public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		userRepository.deleteById(id);
		redirectAttributes.addFlashAttribute("success", "🗑️ Xóa user thành công!");
		return "redirect:/admin/users";
	}

	@GetMapping("/stats")
	public String showUserStats(
			@RequestParam(name = "selectedRole", required = false, defaultValue = "ROLE_USER") String selectedRole,
			Model model) {
		model.addAttribute("pageTitle", "Thống kê người dùng");

		// Tổng số người dùng
		model.addAttribute("totalUsers", userService.countAll());

		// Người dùng đang hoạt động
		model.addAttribute("activeUsers", userService.countByActive(true));

		// Số người dùng theo vai trò cố định
		model.addAttribute("totalAdmins", userService.countByRole("ROLE_ADMIN"));
		model.addAttribute("totalShippers", userService.countByRole("ROLE_SHIPPER"));
		model.addAttribute("totalUsersRole", userService.countByRole("ROLE_USER"));

		// Người dùng đăng ký theo tháng
		model.addAttribute("userStatsByMonth", userService.getMonthlyUserRegistrations());

		// Top hoạt động
		model.addAttribute("topUsers", userService.getTopActiveUsers());

		// Vai trò được chọn
		model.addAttribute("selectedRole", selectedRole);
		model.addAttribute("selectedRoleCount", userService.countByRole(selectedRole));

		return "admin/user-stats";
	}
}
