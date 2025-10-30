package com.oneshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/about")
    public String aboutPage() {
        // Trả về templates/about.html
        return "about";
    }

    @GetMapping("/account/wishlist")
    public String wishlistPage() {
        // Trả về templates/account/wishlist.html
        return "account/wishlist";
    }
}
