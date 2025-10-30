package com.oneshop.controller;

import com.oneshop.entity.OrderAddress;
import com.oneshop.entity.User;
import com.oneshop.service.OrderAddressService;
import com.oneshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminOrderAddressController {

    private final OrderAddressService orderAddressService;
    private final UserRepository userRepository;

    @GetMapping("/users/{userId}/addresses")
    public String getUserAddresses(@PathVariable Long userId, Model model) {
        User user = userRepository.findById(userId).orElseThrow();
        List<OrderAddress> addresses = orderAddressService.getAddressesByUser(userId);
        model.addAttribute("user", user);
        model.addAttribute("addresses", addresses);
        return "admin/user-addresses";
    }

    @GetMapping("/addresses")
    public String listAddresses(Model model) {
        List<OrderAddress> addresses = orderAddressService.getAllAddresses();
        model.addAttribute("addresses", addresses);
        model.addAttribute("pageTitle", "Quản lý Địa chỉ đặt hàng");
        return "admin/address-list";
    }
    
    @PostMapping("/addresses/{addressId}/set-default")
    public String setDefaultAddress(@PathVariable Long addressId) {
        OrderAddress selected = orderAddressService.getById(addressId);
        orderAddressService.setDefaultAddress(selected);
        return "redirect:/admin/users/" + selected.getUser().getUserId() + "/addresses";
    }

    @GetMapping("/addresses/create")
    public String createAddressForm(Model model) {
        model.addAttribute("address", new OrderAddress());
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("pageTitle", "Thêm địa chỉ");
        return "admin/address-form";
    }

    
    @GetMapping("/addresses/{addressId}/edit")
    public String editAddress(@PathVariable Long addressId, Model model) {
        OrderAddress addr = orderAddressService.getById(addressId);
        model.addAttribute("address", addr);
        model.addAttribute("users", userRepository.findAll()); // 👈 thêm dòng này
        model.addAttribute("pageTitle", "Chỉnh sửa địa chỉ");
        return "admin/address-form";
    }


    @PostMapping("/addresses/{addressId}/delete")
    public String deleteAddress(@PathVariable Long addressId) {
        OrderAddress addr = orderAddressService.getById(addressId);
        Long userId = addr.getUser().getUserId();
        orderAddressService.delete(addressId);
        return "redirect:/admin/users/" + userId + "/addresses";
    }

    @PostMapping("/addresses/save")
    public String saveAddress(@ModelAttribute("address") OrderAddress address, Model model) {
        try {
            orderAddressService.save(address);
            return "redirect:/admin/users/" + address.getUser().getUserId() + "/addresses";
        } catch (Exception e) {
            model.addAttribute("address", address);
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("pageTitle", address.getAddressId() == null ? "Thêm địa chỉ" : "Chỉnh sửa địa chỉ");
            model.addAttribute("errorMessage", "Lỗi khi lưu địa chỉ: " + e.getMessage());
            return "admin/address-form";
        }
        
    }
    

} 