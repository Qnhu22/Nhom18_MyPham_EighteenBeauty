package com.oneshop.service.impl;

import com.oneshop.entity.ProductVariant;
import com.oneshop.entity.User;
import com.oneshop.entity.WishlistItem;
import com.oneshop.repository.ProductVariantRepository;
import com.oneshop.repository.WishlistRepository;
import com.oneshop.service.WishlistService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductVariantRepository variantRepository;

    @Override
    public List<WishlistItem> getWishlist(User user) {
        return wishlistRepository.findByUser(user);
    }

    @Override
    public void addToWishlist(User user, Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm."));
        boolean exists = wishlistRepository.existsByUserAndVariant(user, variant);
        if (!exists) {
            WishlistItem item = WishlistItem.builder()
                    .user(user)
                    .variant(variant)
                    .build();
            wishlistRepository.save(item);
        }
    }

    @Override
    public void removeFromWishlist(User user, Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm."));
        wishlistRepository.deleteByUserAndVariant(user, variant);
    }

    @Override
    public void syncWishlist(User user, List<Long> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) return;
        List<ProductVariant> variants = variantRepository.findAllById(variantIds);
        for (ProductVariant v : variants) {
            if (!wishlistRepository.existsByUserAndVariant(user, v)) {
                wishlistRepository.save(WishlistItem.builder()
                        .user(user)
                        .variant(v)
                        .build());
            }
        }
    }
}
