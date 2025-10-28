package com.oneshop.repository;

import com.oneshop.entity.Cart;
import com.oneshop.entity.CartItem;
import com.oneshop.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProductVariant(Cart cart, ProductVariant variant);
}
