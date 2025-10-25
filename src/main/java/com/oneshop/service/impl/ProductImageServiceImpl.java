package com.oneshop.service.impl;

import com.oneshop.entity.Product;
import com.oneshop.entity.ProductImage;
import com.oneshop.repository.ProductImageRepository;
import com.oneshop.service.ProductImageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository repository;

    public ProductImageServiceImpl(ProductImageRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ProductImage> getAllImages() {
        return repository.findAll();
    }

    @Override
    public Optional<ProductImage> getImageById(Long id) {
        return repository.findById(id);
    }

    @Override
    public ProductImage createImage(ProductImage image) {
        return repository.save(image);
    }

    @Override
    public ProductImage updateImage(Long id, ProductImage image) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setImageUrl(image.getImageUrl()); // ✅ sửa lại
                    existing.setProduct(image.getProduct());
                    return repository.save(existing);
                }).orElseThrow(() -> new RuntimeException("Image not found"));
    }

    @Override
    public void deleteImage(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<ProductImage> getImagesByProduct(Product product) {
        return repository.findByProduct(product);
    }
}
