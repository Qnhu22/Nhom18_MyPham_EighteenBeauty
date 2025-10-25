package com.oneshop.service;

import com.oneshop.entity.Shop;
import java.util.List;
import java.util.Optional;

public interface ShopService {
    List<Shop> getAllShops();
    Optional<Shop> getShopById(Long id);
    Shop createShop(Shop shop);
    Shop updateShop(Long id, Shop shop);
    void deleteShop(Long id);
}
