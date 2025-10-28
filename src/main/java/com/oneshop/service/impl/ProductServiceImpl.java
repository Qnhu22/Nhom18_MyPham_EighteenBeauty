package com.oneshop.service.impl;

import com.oneshop.entity.Category;
import com.oneshop.entity.Product;
import com.oneshop.entity.ProductVariant;
import com.oneshop.repository.ProductRepository;
import com.oneshop.repository.ProductVariantRepository;
import com.oneshop.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    /* =====================================================
       📦 Lấy thông tin sản phẩm
       ===================================================== */
    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm để xóa"));

        // Xóa variant trước (tránh lỗi khóa ngoại)
        List<ProductVariant> variants = variantRepository.findByProduct(product);
        variantRepository.deleteAll(variants);

        productRepository.delete(product);
    }

    /* =====================================================
       🏠 Feed trang chủ
       ===================================================== */
    /** 🏆 Sản phẩm nổi bật (Top bán chạy) */
    @Override
    public List<Product> getHighlightedProducts() {
        return productRepository.findBestSellingProducts();
    }

    /** 🔥 Top bán chạy */
    @Override
    public List<Product> getBestSellerProducts() {
        return productRepository.findBestSellingProducts();
    }

    /** 🆕 Sản phẩm mới nhất */
    @Override
    public List<Product> getNewProducts() {
        return productRepository.findTop8ByOrderByCreatedAtDesc();
    }

    /** 💰 Sản phẩm giá tốt (giảm giá hoặc variant có oldPrice > price) */
    @Override
    public List<Product> getBestDeals() {
        return productRepository.findAll().stream()
                .filter(p -> p.getVariants() != null && p.getVariants().stream()
                        .anyMatch(v -> v.getOldPrice() != null && v.getOldPrice().compareTo(v.getPrice()) > 0))
                .limit(8)
                .toList();
    }

    /** 👥 Feed cho khách (guest) */
    @Override
    public List<Product> getTopProductsForGuest() {
        return productRepository.findTopProductsForGuest();
    }

    /* =====================================================
       🔍 Tìm kiếm & phân trang
       ===================================================== */
    @Override
    public Page<Product> getProductsByName(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    @Override
    public Page<Product> getProductsByCategory(Category category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByCategory(category, pageable);
    }

    @Override
    public Page<Product> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> getProductsByStatus(Boolean status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByStatus(status, pageable);
    }

    /* =====================================================
       ⚙️ Quản lý sản phẩm
       ===================================================== */
    @Override
    public List<Product> getBestSellingProducts() {
        return productRepository.findBestSellingProducts();
    }

    @Override
    public Product saveProduct(Product product) {
        // 🧩 Lưu product
        Product savedProduct = productRepository.save(product);

        // 🧩 Lưu variant kèm theo (nếu có)
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            for (ProductVariant variant : product.getVariants()) {
                variant.setProduct(savedProduct);
                if (variant.getCreatedAt() == null) {
                    variant.setCreatedAt(LocalDateTime.now());
                }
                variantRepository.save(variant);
            }
        }

        return savedProduct;
    }

    @Override
    public boolean existsById(Long productId) {
        return productRepository.existsById(productId);
    }

    /** Lấy danh sách variant theo sản phẩm */
    @Override
    public List<ProductVariant> getVariantsByProduct(Product product) {
        return variantRepository.findByProduct(product);
    }

    /* =====================================================
       💡 Hỗ trợ hiển thị giá min–max
       ===================================================== */
    @Override
    public String getPriceRange(Product product) {
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            return "₫0";
        }

        double min = product.getVariants().stream()
                .filter(v -> v.getPrice() != null)
                .map(v -> v.getPrice().doubleValue())
                .min(Comparator.naturalOrder())
                .orElse(0.0);

        double max = product.getVariants().stream()
                .filter(v -> v.getPrice() != null)
                .map(v -> v.getPrice().doubleValue())
                .max(Comparator.naturalOrder())
                .orElse(min);

        if (min == max) {
            return String.format("₫%,.0f", min);
        } else {
            return String.format("₫%,.0f - ₫%,.0f", min, max);
        }
    }
}
