package com.oneshop.service;

import com.oneshop.entity.Shop;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

public interface ShopService {
    List<Shop> getAllShops();
    Optional<Shop> getShopById(Long id);
    Shop createShop(Shop shop);
    //Shop updateShop(Long id, Shop shop);
    void deleteShop(Long id);
    Shop updateShop(Shop shop, MultipartFile logoFile) throws IOException;
	Shop getSingleShop();
}
