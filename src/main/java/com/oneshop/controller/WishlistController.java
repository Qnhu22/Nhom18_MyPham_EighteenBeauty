package com.oneshop.controller;

import com.oneshop.entity.User;
import com.oneshop.entity.WishlistItem;
import com.oneshop.security.UserPrincipal;
import com.oneshop.service.WishlistService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    // 📦 Lấy danh sách yêu thích
    @GetMapping
    public List<WishlistDto> getWishlist(@AuthenticationPrincipal UserPrincipal principal) {
        User user = principal.getUser();
        List<WishlistItem> items = wishlistService.getWishlist(user);
        return items.stream().map(WishlistDto::fromEntity).toList();
    }

    @PostMapping("/{variantId}")
    public void addToWishlist(@PathVariable Long variantId,
                              @AuthenticationPrincipal UserPrincipal principal) {
        wishlistService.addToWishlist(principal.getUser(), variantId);
    }

    @DeleteMapping("/{variantId}")
    public void removeFromWishlist(@PathVariable Long variantId,
                                   @AuthenticationPrincipal UserPrincipal principal) {
        wishlistService.removeFromWishlist(principal.getUser(), variantId);
    }

    @PostMapping("/sync")
    public void syncWishlist(@RequestBody SyncRequest body,
                             @AuthenticationPrincipal UserPrincipal principal) {
        wishlistService.syncWishlist(principal.getUser(), body.getVariantIds());
    }

    // ======================= DTO nội bộ =======================
    @Data
    public static class SyncRequest {
        private List<Long> variantIds;
    }

    @Data
    public static class WishlistDto {
        private Long variantId;
        private String name;
        private String imageUrl;
        private String priceText;

        public static WishlistDto fromEntity(WishlistItem item) {
            var variant = item.getVariant();
            var product = variant.getProduct();

            WishlistDto dto = new WishlistDto();
            dto.variantId = variant.getVariantId();
            dto.name = product.getName() +
                    (variant.getColor() != null ? " - " + variant.getColor() : "") +
                    (variant.getSize() != null ? " / " + variant.getSize() : "");
            dto.imageUrl = variant.getImageUrl() != null ?
                    variant.getImageUrl() :
                    product.getImageUrl();
            dto.priceText = String.format("%,d ₫", variant.getPrice().longValue());
            return dto;
        }
    }
}
