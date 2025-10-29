package com.oneshop.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.oneshop.enums.DiscountType;
import com.oneshop.enums.VoucherStatus;
import com.oneshop.service.VoucherService;
import com.oneshop.entity.Voucher;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/manager/vouchers")
@RequiredArgsConstructor
public class ManagerVoucherController {
	
	private final VoucherService service;

    @GetMapping
    public String list(
            @RequestParam(required = false) VoucherStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) DiscountType discountType,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<Voucher> vouchers = service.filterVouchers(keyword, discountType, status, page);
        model.addAttribute("vouchers", vouchers.getContent());

        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedType", discountType);
        model.addAttribute("allTypes", DiscountType.values());
        model.addAttribute("allStatuses", VoucherStatus.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", vouchers.getTotalPages());

        return "manager/voucher/voucher-list";
    }

    @GetMapping({"/add", "/edit/{id}"})
    public String form(@PathVariable(required = false) Long id,
                        Model model) {
        Voucher v = id != null ?
                service.getById(id).orElse(new Voucher()) :
                new Voucher();
        model.addAttribute("voucher", v);
        model.addAttribute("allTypes", DiscountType.values());
        model.addAttribute("allStatuses", VoucherStatus.values());       
        model.addAttribute("actionUrl", "/manager/vouchers/save");
        return "manager/voucher/voucher-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Voucher voucher,
                       RedirectAttributes redirect) {
        service.saveOrUpdate(voucher);
        redirect.addFlashAttribute("success", "Lưu thành công!");
        return "redirect:/manager/vouchers";
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/detail/{id}")
    @ResponseBody
    public ResponseEntity<?> detail(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
    }
}
