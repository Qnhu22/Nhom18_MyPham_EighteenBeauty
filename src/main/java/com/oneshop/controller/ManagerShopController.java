package com.oneshop.controller;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.oneshop.entity.Shop;

import com.oneshop.service.ShopService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/manager/shop")
@RequiredArgsConstructor
public class ManagerShopController {
	@Autowired
    private ShopService shopService;

    // Hiển thị trang shop
    @GetMapping
    public String shopInfo(Model model) {
        Shop shop = shopService.getSingleShop();
        model.addAttribute("shop", shop);
        return "manager/shop-info"; // file HTML trong templates/manager/
    }
    
    @ModelAttribute("shop")
    public Shop shop() {
        return shopService.getSingleShop();
    }

    // Cập nhật thông tin shop
    @PostMapping("/update")
    public String updateShop(@ModelAttribute Shop shop,
                             @RequestParam("logoFile") MultipartFile logoFile,
                             Model model) {
        try {
            shopService.updateShop(shop, logoFile);
            model.addAttribute("success", "Cập nhật thông tin cửa hàng thành công!");
        } catch (IOException e) {
            model.addAttribute("error", "Lỗi khi upload logo: " + e.getMessage());
            e.printStackTrace();
        }
        model.addAttribute("shop", shopService.getSingleShop());
        return "manager/shop-info";
    }
}
