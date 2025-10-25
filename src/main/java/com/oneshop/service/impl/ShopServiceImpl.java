package com.oneshop.service.impl;

import com.oneshop.entity.Shop;
import com.oneshop.repository.ShopRepository;
import com.oneshop.service.ShopService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;

    public ShopServiceImpl(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    @Override
    public List<Shop> getAllShops() {
        return shopRepository.findAll();
    }

    @Override
    public Optional<Shop> getShopById(Long id) {
        return shopRepository.findById(id);
    }

    @Override
    public Shop createShop(Shop shop) {
        return shopRepository.save(shop);
    }

    @Override
    public Shop updateShop(Long id, Shop shop) {
        return shopRepository.findById(id)
                .map(existing -> {
                    existing.setName(shop.getName());
                    existing.setDescription(shop.getDescription());
                    existing.setLogoUrl(shop.getLogoUrl());
                    existing.setEmail(shop.getEmail());
                    existing.setPhone(shop.getPhone());
                    existing.setAddress(shop.getAddress());
                    existing.setStatus(shop.isStatus());
                    return shopRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
    }

    @Override
    public void deleteShop(Long id) {
        shopRepository.deleteById(id);
    }
}
