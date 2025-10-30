package com.oneshop.repository;

import com.oneshop.entity.WishlistItem;
import com.oneshop.entity.User;
import com.oneshop.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUser(User user);
    Optional<WishlistItem> findByUserAndVariant(User user, ProductVariant variant);
    boolean existsByUserAndVariant(User user, ProductVariant variant);
    void deleteByUserAndVariant(User user, ProductVariant variant);
}
