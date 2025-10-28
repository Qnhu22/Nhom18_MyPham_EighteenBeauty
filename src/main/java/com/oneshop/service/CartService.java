package com.oneshop.service;

import com.oneshop.entity.User;
import com.oneshop.entity.Cart;
import com.oneshop.entity.CartItem;
import com.oneshop.entity.ProductVariant;

import java.util.List;

public interface CartService {

    /** 🛒 Lấy giỏ hàng của user, nếu chưa có thì tạo mới */
    Cart getOrCreateCart(User user);

    /** ➕ Thêm sản phẩm (variant) vào giỏ */
    void addToCart(User user, Long variantId, int quantity);

    /** 🗑 Xóa 1 item trong giỏ */
    void removeItem(User user, Long cartItemId);

    /** 🧹 Xóa toàn bộ giỏ hàng */
    void clearCart(User user);

    /** 🔹 Lấy danh sách các CartItem của user (phục vụ mini-cart) */
    List<CartItem> getCartItems(User user);

    /** 💰 Tính tổng giá trị giỏ hàng */
    double calculateTotal(User user);

    /** 🔢 Đếm số lượng item trong giỏ */
    int countItems(User user);
}
