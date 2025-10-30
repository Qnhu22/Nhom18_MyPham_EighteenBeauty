package com.oneshop.controller;

import com.oneshop.entity.User;
import com.oneshop.entity.Voucher;
import com.oneshop.security.UserPrincipal;
import com.oneshop.service.CartService;
import com.oneshop.service.VoucherService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;
    private final CartService cartService;

    @GetMapping("/account/vouchers")
    public String viewVouchers(@AuthenticationPrincipal UserPrincipal principal,
                               org.springframework.ui.Model model) {
        List<Voucher> vouchers = voucherService.getAllVouchers();
        BigDecimal cartTotal = BigDecimal.ZERO;
        if (principal != null && principal.getUser() != null) {
            cartTotal = cartService.getCartItems(principal.getUser()).stream()
                    .map(ci -> ci.getProductVariant().getPrice()
                            .multiply(BigDecimal.valueOf(ci.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        model.addAttribute("vouchers", vouchers);
        model.addAttribute("cartTotal", cartTotal); // 👈 thêm
        return "account/voucher-wallet";
    }


    // ✅ Khi user chọn “Chọn voucher”
    @GetMapping("/account/vouchers/apply/{id}")
    public String applyVoucher(@PathVariable Long id,
                               @AuthenticationPrincipal UserPrincipal principal,
                               HttpSession session,
                               RedirectAttributes ra) {

        Voucher selected = voucherService.getAllVouchers()
                .stream()
                .filter(v -> v.getVoucherId().equals(id))
                .findFirst()
                .orElse(null);

        if (selected == null) {
            ra.addFlashAttribute("err", "❌ Voucher không tồn tại.");
            return "redirect:/account/vouchers";
        }

        if (principal == null || principal.getUser() == null) {
            ra.addFlashAttribute("err", "Vui lòng đăng nhập để sử dụng voucher.");
            return "redirect:/login";
        }

        User user = principal.getUser();

        // ✅ Tính tổng tiền giỏ hàng hiện tại
        BigDecimal cartTotal = cartService.getCartItems(user).stream()
                .map(ci -> ci.getProductVariant().getPrice()
                        .multiply(BigDecimal.valueOf(ci.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ✅ Kiểm tra đơn tối thiểu
        if (selected.getMinOrderValue() != null &&
            cartTotal.compareTo(selected.getMinOrderValue()) < 0) {

            ra.addFlashAttribute("err",
                    "⚠️ Đơn hàng của bạn (" +
                    String.format("%,.0f", cartTotal) +
                    "₫) chưa đạt giá trị tối thiểu (" +
                    String.format("%,.0f", selected.getMinOrderValue()) +
                    "₫) để dùng voucher này.");
            return "redirect:/account/vouchers";
        }

        // ✅ Nếu đủ điều kiện thì lưu voucher vào session
        session.setAttribute("selectedVoucher", selected);
        ra.addFlashAttribute("msg", "✅ Đã áp dụng voucher: " + selected.getCode());

        return "redirect:/checkout";
    }

    @GetMapping("/account/vouchers/remove")
    public String removeVoucher(HttpSession session, RedirectAttributes ra) {
        session.removeAttribute("selectedVoucher");
        ra.addFlashAttribute("msg", "🗑️ Đã bỏ chọn voucher.");
        return "redirect:/checkout";
    }
}
