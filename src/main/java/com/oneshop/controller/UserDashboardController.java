package com.oneshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserDashboardController {

    @GetMapping("/user/dashboard")
    public String userDashboard(Model model) {
        model.addAttribute("pageTitle", "User Dashboard");
        return "dashboard/user-dashboard"; // trỏ tới templates/dashboard/user-dashboard.html
    }
}
