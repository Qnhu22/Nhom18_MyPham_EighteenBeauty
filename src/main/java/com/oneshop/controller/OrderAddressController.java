package com.oneshop.controller;

import com.oneshop.entity.OrderAddress;
import com.oneshop.entity.User;
import com.oneshop.service.OrderAddressService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account/addresses")
@RequiredArgsConstructor
public class OrderAddressController {

    private final OrderAddressService addressService;

    @GetMapping
    public String showAddresses(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("addresses", addressService.getAddressesByUser(user));
        return "account/addresses";
    }

    @PostMapping("/save")
    public String saveAddress(@ModelAttribute OrderAddress address,
                              HttpSession session,
                              RedirectAttributes redirectAttrs) {
    	System.out.println("✅ isDefault gửi lên: " + address.isDefault());
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        boolean isUpdate = (address.getAddressId() != null);
        addressService.saveAddress(address, user);

        if (isUpdate)
            redirectAttrs.addFlashAttribute("msgSuccess", "✅ Cập nhật địa chỉ thành công!");
        else
            redirectAttrs.addFlashAttribute("msgSuccess", "✅ Thêm địa chỉ mới thành công!");

        return "redirect:/account/addresses";
    }

    @PostMapping("/delete/{id}")
    public String deleteAddress(@PathVariable Long id,
                                HttpSession session,
                                RedirectAttributes redirectAttrs) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            addressService.deleteAddress(id, user);
            redirectAttrs.addFlashAttribute("msgSuccess", "🗑️ Đã xóa địa chỉ thành công!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("msgError", "❌ Không thể xóa địa chỉ!");
        }

        return "redirect:/account/addresses";
    }

    @PostMapping("/set-default/{id}")
    public String setDefault(@PathVariable Long id,
                             HttpSession session,
                             RedirectAttributes redirectAttrs) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        addressService.setDefaultAddress(id, user);
        redirectAttrs.addFlashAttribute("msgSuccess", "⭐ Đã đặt làm địa chỉ mặc định!");
        return "redirect:/account/addresses";
    }

    // API lấy địa chỉ để sửa (dùng AJAX)
    @ResponseBody
    @GetMapping("/{id}")
    public OrderAddress getAddressById(@PathVariable Long id) {
        return addressService.getAddressById(id);
    }
}
