package com.oneshop.service.impl;

import com.oneshop.entity.*;
import com.oneshop.repository.*;
import com.oneshop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;

    // 🛒 Lấy hoặc tạo giỏ hàng cho user
    @Override
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build(); // ✅ không cần .items()
                    return cartRepository.save(newCart);
                });
    }



    // ➕ Thêm sản phẩm vào giỏ
    @Override
    public void addToCart(User user, Long variantId, int quantity) {
        Cart cart = getOrCreateCart(user);
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID " + variantId));

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductVariant().getVariantId().equals(variantId))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productVariant(variant)
                    .quantity(quantity)
                    .priceAtAdd(variant.getPrice())
                    .build();
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
    }

    // 🗑 Xóa 1 sản phẩm khỏi giỏ (dựa vào variantId)
    @Override
    public void removeItem(User user, Long variantId) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().removeIf(item ->
                item.getProductVariant().getVariantId().equals(variantId));
        cartRepository.save(cart);
    }

    // 🧹 Xóa toàn bộ giỏ hàng
    @Override
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    // 🔹 Lấy danh sách CartItem để hiển thị mini cart
    @Override
    public List<CartItem> getCartItems(User user) {
        Cart cart = cartRepository.findByUser(user).orElse(null);
        return (cart != null) ? List.copyOf(cart.getItems()) : List.of();
    }

    // 💰 Tính tổng giá trị giỏ hàng
    @Override
    public double calculateTotal(User user) {
        return getCartItems(user).stream()
                .mapToDouble(item -> item.getProductVariant().getPrice().doubleValue() * item.getQuantity())
                .sum();
    }

    // 🔢 Đếm số lượng item (cho badge icon)
    @Override
    public int countItems(User user) {
        return getCartItems(user).size();
    }
}
