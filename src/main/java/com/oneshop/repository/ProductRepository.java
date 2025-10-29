package com.oneshop.repository;

import com.oneshop.entity.Category;
import com.oneshop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // 🏆 Top sản phẩm bán chạy (dựa trên tổng soldCount của các variant)
    @Query("""
        SELECT p 
        FROM Product p 
        JOIN p.variants v 
        GROUP BY p 
        ORDER BY SUM(v.soldCount) DESC
    """)
    List<Product> findBestSellingProducts();

    // 🌸 Sản phẩm mới nhất
    List<Product> findTop8ByOrderByCreatedAtDesc();

    // 💰 Sản phẩm giá tốt (tùy theo cột price trong ProductVariant hoặc Product)
    // Nếu Product không có price, có thể bỏ dòng này
    // List<Product> findTop8ByOrderByPriceAsc();

    // 🔍 Tìm theo tên
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // 📂 Theo danh mục
    Page<Product> findByCategory(Category category, Pageable pageable);

    // 💡 Dành cho trang guest: chọn sản phẩm có tổng soldCount > 10
    @Query("""
        SELECT p 
        FROM Product p 
        JOIN p.variants v 
        GROUP BY p 
        HAVING SUM(v.soldCount) > 10 
        ORDER BY SUM(v.soldCount) DESC
    """)
    List<Product> findTopProductsForGuest();

    // ⚙️ Lấy tất cả sản phẩm (phân trang)
    @Override
    Page<Product> findAll(Pageable pageable);

    // ⚙️ Lọc theo trạng thái hiển thị
    Page<Product> findByStatus(Boolean status, Pageable pageable);

    // ⚙️ Lấy thông tin chi tiết sản phẩm
    Product findByProductId(Long productId);
}
