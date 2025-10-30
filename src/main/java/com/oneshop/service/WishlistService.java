package com.oneshop.service;

import com.oneshop.entity.User;
import com.oneshop.entity.WishlistItem;

import java.util.List;

public interface WishlistService {
    List<WishlistItem> getWishlist(User user);
    void addToWishlist(User user, Long variantId);
    void removeFromWishlist(User user, Long variantId);
    void syncWishlist(User user, List<Long> variantIds);
}
