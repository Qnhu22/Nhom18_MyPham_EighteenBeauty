package com.oneshop.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.oneshop.entity.Review;
import com.oneshop.repository.ProductRepository;
import com.oneshop.service.ReviewService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/manager/reviews")
@RequiredArgsConstructor
public class ManagerReviewController {
	private final ReviewService service;
    private final ProductRepository productRepo; // để hiển thị combobox sản phẩm

    @GetMapping
    public String list(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<Review> reviews = service.filterReviews(productId, status, rating, fromDate, toDate, keyword, page);

        model.addAttribute("reviews", reviews.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviews.getTotalPages());

        model.addAttribute("selectedProductId", productId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedRating", rating);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("keyword", keyword);

        model.addAttribute("allProducts", productRepo.findAll());
        model.addAttribute("allRatings", List.of(1,2,3,4,5));

        return "manager/review/review-list";
    }

    @PostMapping("/reply/{id}")
    public String reply(@PathVariable Long id, @RequestParam String reply, RedirectAttributes redirect) {
        Review r = service.getById(id).orElseThrow();
        r.setReply(reply);
        r.setReplyDate(LocalDateTime.now());
        service.saveOrUpdate(r);
        redirect.addFlashAttribute("success", "Phản hồi đã được lưu!");
        return "redirect:/manager/reviews";
    }

    @PostMapping("/toggle-status/{id}")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        Review r = service.getById(id).orElseThrow();
        r.setStatus(!r.isStatus());
        service.saveOrUpdate(r);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok("OK");
    }
}
