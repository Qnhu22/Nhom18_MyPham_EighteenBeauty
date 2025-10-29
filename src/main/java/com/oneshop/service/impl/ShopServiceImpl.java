package com.oneshop.service.impl;

import com.oneshop.entity.Shop;
import com.oneshop.repository.ShopRepository;
import com.oneshop.service.ShopService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ShopServiceImpl implements ShopService {

	@Autowired
	private ShopRepository shopRepository;
	private final String UPLOAD_DIR = "uploads/logo/";

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

//    @Override
//    public Shop updateShop(Long id, Shop shop) {
//        return shopRepository.findById(id)
//                .map(existing -> {
//                    existing.setName(shop.getName());
//                    existing.setDescription(shop.getDescription());
//                    existing.setLogoUrl(shop.getLogoUrl());
//                    existing.setEmail(shop.getEmail());
//                    existing.setPhone(shop.getPhone());
//                    existing.setAddress(shop.getAddress());
//                    existing.setStatus(shop.isStatus());
//                    return shopRepository.save(existing);
//                })
//                .orElseThrow(() -> new RuntimeException("Shop không tồn tại"));
//    }

    @Override
    public void deleteShop(Long id) {
        shopRepository.deleteById(id);
    }
    @Override
	public Shop updateShop(Shop shop, MultipartFile logoFile) throws IOException {
		Shop existing = shopRepository.getSingleShop();
		if (existing == null) {
			existing = new Shop();
		}

		existing.setName(shop.getName());
		existing.setEmail(shop.getEmail());
		existing.setPhone(shop.getPhone());
		existing.setAddress(shop.getAddress());
		existing.setDescription(shop.getDescription());

		if (logoFile != null && !logoFile.isEmpty()) {
			String filename = UUID.randomUUID() + "_" + logoFile.getOriginalFilename();
			Path uploadPath = Paths.get(UPLOAD_DIR);
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}
			Path filePath = uploadPath.resolve(filename);
			Files.copy(logoFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
			existing.setLogoUrl("/uploads/logo" + filename);
		}

		return shopRepository.save(existing);
	}

	@Override
	public Shop getSingleShop() {
		return shopRepository.findAll()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    // Nếu chưa có shop nào, trả về object trống
                    Shop shop = new Shop();
                    shop.setName("");
                    shop.setEmail("");
                    shop.setPhone("");
                    shop.setAddress("");
                    shop.setDescription("");
                    return shop;
                });
	}
}
