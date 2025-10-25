package com.oneshop.service;

import com.oneshop.entity.Product;
import com.oneshop.entity.ProductImage;

import java.util.List;
import java.util.Optional;

public interface ProductImageService {
    List<ProductImage> getAllImages();
    Optional<ProductImage> getImageById(Long id);
    ProductImage createImage(ProductImage image);
    ProductImage updateImage(Long id, ProductImage image);
    void deleteImage(Long id);

    // Thêm tiện ích: lấy ảnh theo sản phẩm
    List<ProductImage> getImagesByProduct(Product product);
}
