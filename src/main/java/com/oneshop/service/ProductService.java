package com.oneshop.service;

import com.oneshop.entity.Category;
import com.oneshop.entity.Product;
import com.oneshop.entity.ProductVariant;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    // 🏠 Feed cho trang chủ
    List<Product> getHighlightedProducts();
    List<Product> getBestSellerProducts();
    List<Product> getNewProducts();
    List<Product> getBestDeals();

    // 👥 Cho khách (guest)
    List<Product> getTopProductsForGuest();

    // 🔍 Tìm kiếm & phân trang
    Page<Product> getProductsByName(String keyword, int page, int size);
    Page<Product> getProductsByCategory(Category category, int page, int size);
    Page<Product> getAllProducts(int page, int size);
    Page<Product> getProductsByStatus(Boolean status, int page, int size);
    
    Page<Product> getAllProducts(int page, int size, String sort);
    Page<Product> getProductsByCategory(Category category, int page, int size, String sort);

    // 📦 Quản lý sản phẩm
    Optional<Product> getProductById(Long productId);
    List<Product> getBestSellingProducts();
    Product saveProduct(Product product);
    void deleteProduct(Long productId);
    boolean existsById(Long productId);

    // 🔗 Biến thể
    List<ProductVariant> getVariantsByProduct(Product product);

    // 💡 Hỗ trợ hiển thị giá
    String getPriceRange(Product product);
}
