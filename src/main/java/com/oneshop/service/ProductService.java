package com.oneshop.service;

import com.oneshop.entity.Category;
import com.oneshop.entity.Product;
import com.oneshop.entity.ProductVariant;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

public interface ProductService {
    
    // feed cho trang chủ
    List<Product> getHighlightedProducts();
    List<Product> getBestSellerProducts();
    List<Product> getNewProducts();
    List<Product> getBestDeals();

    // cho guest
    List<Product> getTopProductsForGuest();

    Page<Product> getProductsByName(String keyword, int page, int size);

    Page<Product> getProductsByCategory(Category category, int page, int size);

    Page<Product> getAllProducts(int page, int size);

    Page<Product> getProductsByStatus(Boolean status, int page, int size);

    Optional<Product> getProductById(Long productId);

    List<Product> getBestSellingProducts();

    Product saveProduct(Product product);

    void deleteProduct(Long productId);

    boolean existsById(Long productId);
    
    List<ProductVariant> getVariantsByProduct(Product product);

}
