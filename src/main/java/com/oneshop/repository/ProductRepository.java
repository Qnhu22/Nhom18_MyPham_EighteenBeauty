package com.oneshop.repository;

import com.oneshop.entity.Category;
import com.oneshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /* ================== 🏆 TOP BÁN CHẠY ================== */
    @Query("""
        SELECT p 
        FROM Product p 
        JOIN p.variants v 
        GROUP BY p 
        ORDER BY SUM(v.soldCount) DESC
    """)
    List<Product> findBestSellingProducts();

    /* ================== 🌸 SẢN PHẨM MỚI ================== */
    List<Product> findTop8ByOrderByCreatedAtDesc();

    /* ================== 💸 SẢN PHẨM CÓ GIẢM GIÁ ==================
       (Lưu ý: giảm giá được lấy qua ProductVariant.oldPrice) */
    @Query("""
        SELECT DISTINCT p
        FROM Product p
        JOIN p.variants v
        WHERE v.oldPrice IS NOT NULL
        ORDER BY v.createdAt DESC
    """)
    List<Product> findTop8DiscountedProducts();

    /* ================== 🔍 TÌM THEO TÊN ================== */
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /* ================== 📂 THEO DANH MỤC ================== */
    Page<Product> findByCategory(Category category, Pageable pageable);

    /* ================== 👥 TOP SẢN PHẨM CHO GUEST ================== */
    @Query("""
        SELECT p 
        FROM Product p 
        JOIN p.variants v 
        GROUP BY p 
        HAVING SUM(v.soldCount) > 10 
        ORDER BY SUM(v.soldCount) DESC
    """)
    List<Product> findTopProductsForGuest();

    /* ================== ⚙️ PHÂN TRANG & TRẠNG THÁI ================== */
    @Override
    Page<Product> findAll(Pageable pageable);

    Page<Product> findByStatus(Boolean status, Pageable pageable);

    /* ================== 📦 CHI TIẾT ================== */
    Product findByProductId(Long productId);
}
