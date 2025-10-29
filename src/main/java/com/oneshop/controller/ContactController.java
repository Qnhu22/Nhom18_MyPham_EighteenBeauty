package com.oneshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContactController {
	 @GetMapping("/contact")
	    public String contactPage() {
	        return "contact"; 
	    }
	 
	 @PostMapping("/contact")
	    public String handleContactForm(
	            @RequestParam String firstName,
	            @RequestParam String lastName,
	            @RequestParam String email,
	            @RequestParam String subject,
	            @RequestParam String message,
	            Model model) {

	    

	        model.addAttribute("successMessage", "Cảm ơn bạn đã liên hệ! Chúng tôi sẽ phản hồi sớm nhất.");
	        return "contact"; 
	    }
}
