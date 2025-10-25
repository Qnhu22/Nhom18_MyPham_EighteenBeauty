package com.oneshop.service.impl;

import com.oneshop.entity.Category;
import com.oneshop.entity.Product;
import com.oneshop.entity.ProductVariant;
import com.oneshop.repository.ProductRepository;
import com.oneshop.repository.ProductVariantRepository;
import com.oneshop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, ProductVariantRepository variantRepository) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // Xóa các variant trước để tránh lỗi khóa ngoại
        List<ProductVariant> variants = variantRepository.findByProduct(product);
        variantRepository.deleteAll(variants);

        // Xóa sản phẩm
        productRepository.delete(product);
    }

    // ---------- FEED TRANG CHỦ ----------

    /** 🏆 Sản phẩm nổi bật (bán chạy) */
    @Override
    public List<Product> getHighlightedProducts() {
        return productRepository.findBestSellingProducts(); // tổng soldCount variant
    }

    /** 🏆 Top bán chạy */
    @Override
    public List<Product> getBestSellerProducts() {
        return productRepository.findBestSellingProducts();
    }

    /** 🌸 Sản phẩm mới nhất */
    @Override
    public List<Product> getNewProducts() {
        return productRepository.findTop8ByOrderByCreatedAtDesc();
    }

    /** 💰 Sản phẩm giá tốt (tạm thời dùng theo thời gian, do giá nằm ở variant) */
    @Override
    public List<Product> getBestDeals() {
        return productRepository.findTopProductsForGuest(); // tạm hiển thị top guest
    }

    /** 👥 Feed cho khách (guest) */
    @Override
    public List<Product> getTopProductsForGuest() {
        return productRepository.findTopProductsForGuest();
    }

    // ---------- TÌM KIẾM / PHÂN TRANG ----------

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

    // ---------- QUẢN LÝ ----------

    @Override
    public List<Product> getBestSellingProducts() {
        return productRepository.findBestSellingProducts();
    }

    @Override
    public Product saveProduct(Product product) {
        // Lưu product trước
        Product savedProduct = productRepository.save(product);

        // Sau đó gán lại product cho các variant (nếu có)
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            for (ProductVariant variant : product.getVariants()) {
                variant.setProduct(savedProduct);
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
    public List<ProductVariant> getVariantsByProduct(Product product) {
        return variantRepository.findByProduct(product);
    }
}
