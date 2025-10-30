package com.oneshop.controller;

import com.oneshop.entity.Promotion;
import com.oneshop.service.PromotionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/admin/promotion")
public class PromotionController {

	@Autowired
	private PromotionService promotionService;

	@GetMapping("/list")
	public String showPromotionList(Model model) {
		List<Promotion> promotions = promotionService.getAllPromotions();
		model.addAttribute("promotions", promotions);
		model.addAttribute("pageTitle", "Danh sách khuyến mãi");
		return "admin/promotion-list";
	}

	@GetMapping("/create")
	public String showCreateForm(Model model) {
		model.addAttribute("promotion", new Promotion());
		model.addAttribute("pageTitle", "Thêm khuyến mãi mới");
		return "admin/promotion-form";
	}

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable("id") Long id, Model model) {
		Promotion promotion = promotionService.getPromotionById(id);
		if (promotion == null) {
			return "redirect:/admin/promotion/list";
		}
		model.addAttribute("promotion", promotion);
		model.addAttribute("pageTitle", "Cập nhật khuyến mãi");
		return "admin/promotion-form";
	}

	@PostMapping("/save")
	public String savePromotion(@ModelAttribute("promotion") Promotion promotion) {
		promotionService.savePromotion(promotion);
		return "redirect:/admin/promotion/list";
	}

	@GetMapping("/delete/{id}")
	public String deletePromotion(@PathVariable("id") Long id) {
		promotionService.deletePromotion(id);
		return "redirect:/admin/promotion/list";
	}

	@GetMapping("/search")
	public String searchPromotion(@RequestParam("keyword") String keyword, Model model) {
		List<Promotion> promotions = promotionService.searchByNameOrType(keyword);
		model.addAttribute("promotions", promotions);
		model.addAttribute("pageTitle", "Tìm kiếm khuyến mãi");
		return "admin/promotion-list";
	}
}
