package com.oneshop.controller;

import com.oneshop.entity.*;
import com.oneshop.enums.DiscountType;
import com.oneshop.enums.OrderStatus;
import com.oneshop.repository.UserRepository;
import com.oneshop.security.UserPrincipal;
import com.oneshop.service.CartService;
import com.oneshop.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.oneshop.repository.ProductVariantRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CheckoutController {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;

    // =======================
    // GET: Trang xác nhận thanh toán
    // =======================
    @GetMapping("/checkout")
    public String showCheckout(@AuthenticationPrincipal UserPrincipal principal,
                               Model model,
                               HttpSession session) {
        if (principal == null || principal.getUser() == null) {
            return "redirect:/login";
        }

        // ⚠️ Nên lấy lại user từ DB để tránh entity bị detached/lazy
        User user = userRepository.findById(principal.getUser().getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user trong DB!"));

        // 🔹 Lấy địa chỉ mặc định (dựa vào boolean isDefault)
        OrderAddress defaultAddress = null;
        if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
            defaultAddress = user.getAddresses()
                    .stream()
                    .filter(OrderAddress::isDefaultAddress)
                    .findFirst()
                    .orElse(user.getAddresses().stream().findFirst().orElse(null));
        }

        // 🔹 Lấy giỏ hàng
        List<CartItem> items = cartService.getCartItems(user);

        // 🔹 Tính tạm tính
        BigDecimal subtotal = items.stream()
                .map(i -> i.getProductVariant().getPrice()
                        .multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 🔹 Phí ship mặc định
        BigDecimal shipFee = BigDecimal.valueOf(25000);

        // 🔹 Voucher + giảm giá
        Voucher selectedVoucher = (Voucher) session.getAttribute("selectedVoucher");
        BigDecimal discount = BigDecimal.ZERO;

        if (selectedVoucher != null) {
            if (selectedVoucher.getDiscountType() == DiscountType.PERCENT) {
                discount = subtotal.multiply(BigDecimal.valueOf(selectedVoucher.getDiscountPercent()))
                        .divide(BigDecimal.valueOf(100));
                if (selectedVoucher.getMaxDiscountValue() != null
                        && discount.compareTo(selectedVoucher.getMaxDiscountValue()) > 0) {
                    discount = selectedVoucher.getMaxDiscountValue();
                }
            } else if (selectedVoucher.getDiscountType() == DiscountType.AMOUNT) {
                discount = selectedVoucher.getDiscountAmount();
            } else if (selectedVoucher.getDiscountType() == DiscountType.FREESHIP) {
                shipFee = BigDecimal.ZERO;
            }
        }

        BigDecimal total = subtotal.add(shipFee).subtract(discount);

        // 🔹 Đẩy dữ liệu ra view
        model.addAttribute("defaultAddress", defaultAddress);
        model.addAttribute("items", items);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shipFee", shipFee);
        model.addAttribute("discount", discount);
        model.addAttribute("total", total);
        model.addAttribute("selectedVoucher", selectedVoucher);

        return "account/checkout"; // ✅ đúng đường dẫn file templates/account/checkout.html
    }

    // =======================
    // POST: Xác nhận đặt hàng
    // =======================
    @PostMapping("/checkout/confirm")
    public String confirm(@AuthenticationPrincipal UserPrincipal principal,
                          @RequestParam(value = "paymentMethod", required = false, defaultValue = "COD") String paymentMethod,
                          RedirectAttributes ra,
                          HttpSession session) {

        if (principal == null || principal.getUser() == null) {
            ra.addFlashAttribute("err", "Vui lòng đăng nhập lại.");
            return "redirect:/login";
        }

        User user = userRepository.findById(principal.getUser().getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user trong DB!"));

        List<CartItem> cartItems = cartService.getCartItems(user);
        if (cartItems.isEmpty()) {
            ra.addFlashAttribute("err", "Giỏ hàng trống.");
            return "redirect:/cart";
        }

        // ✅ Tổng tiền hàng (chưa ship, chưa giảm)
        BigDecimal total = cartItems.stream()
                .map(ci -> ci.getProductVariant().getPrice()
                        .multiply(BigDecimal.valueOf(ci.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shipFee = BigDecimal.valueOf(25000);
        BigDecimal discount = BigDecimal.ZERO;

        Voucher selectedVoucher = (Voucher) session.getAttribute("selectedVoucher");

        // ✅ Áp dụng voucher
        if (selectedVoucher != null) {
            if (selectedVoucher.getDiscountType() == DiscountType.PERCENT) {
                discount = total.multiply(BigDecimal.valueOf(selectedVoucher.getDiscountPercent()))
                        .divide(BigDecimal.valueOf(100));
                if (selectedVoucher.getMaxDiscountValue() != null
                        && discount.compareTo(selectedVoucher.getMaxDiscountValue()) > 0) {
                    discount = selectedVoucher.getMaxDiscountValue();
                }
            } else if (selectedVoucher.getDiscountType() == DiscountType.AMOUNT) {
                discount = selectedVoucher.getDiscountAmount();
            } else if (selectedVoucher.getDiscountType() == DiscountType.FREESHIP) {
                shipFee = BigDecimal.ZERO;
            }
        }

        BigDecimal finalAmount = total.add(shipFee).subtract(discount);

        // ✅ Thanh toán online coi là PAID, COD là UNPAID
        String paymentStatus = (paymentMethod.equalsIgnoreCase("MOMO")
                || paymentMethod.equalsIgnoreCase("BANK")) ? "PAID" : "UNPAID";

        // ✅ Tạo Order
        Order order = Order.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.NEW)
                .totalAmount(total)
                .shippingFee(shipFee)
                .finalAmount(finalAmount)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .productVoucher(selectedVoucher)
                .orderItems(new ArrayList<>())
                .build();

        for (CartItem ci : cartItems) {
            ProductVariant variant = ci.getProductVariant();
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(variant.getProduct())
                    .productVariant(variant)
                    .price(variant.getPrice())
                    .quantity(ci.getQuantity())
                    .subtotal(variant.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                    .discount(BigDecimal.ZERO)
                    .build();
            order.getOrderItems().add(item);
        }

     // ✅ Lưu Order + dọn giỏ
        orderService.saveOrder(order);

        // ✅ Trừ hàng trong kho sau khi đặt hàng thành công
        for (CartItem ci : cartItems) {
            ProductVariant variant = ci.getProductVariant();
            if (variant != null && variant.getStock() != null) {
                int currentStock = variant.getStock();

                // ⚠️ Kiểm tra tồn kho trước khi trừ
                if (ci.getQuantity() > currentStock) {
                    ra.addFlashAttribute("err",
                        "Sản phẩm '" + variant.getName() + "' không đủ hàng (" + currentStock + " còn lại).");
                    return "redirect:/cart";
                }

                // ✅ Giảm kho và lưu DB
                int newStock = Math.max(0, currentStock - ci.getQuantity());
                variant.setStock(newStock);
                productVariantRepository.save(variant);
            }
        }

        cartService.clearCart(user);

        // ✅ Xóa voucher khỏi session sau khi dùng
        session.removeAttribute("selectedVoucher");

        ra.addFlashAttribute("msg", "✅ Đặt hàng thành công! Mã đơn #" + order.getOrderId());
        return "redirect:/account/orders";
    }
}
