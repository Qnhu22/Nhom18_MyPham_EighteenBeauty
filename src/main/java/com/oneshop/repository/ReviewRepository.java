package com.oneshop.repository;

import com.oneshop.entity.Review;
import com.oneshop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 🟢 Lấy các review hiển thị được theo sản phẩm (mới nhất trước)
    List<Review> findByProductAndStatusTrueOrderByCreatedAtDesc(Product product);

    // 🟢 Tính trung bình rating cho sản phẩm
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product = :product AND r.status = true")
    Double findAverageRatingByProduct(@Param("product") Product product);
}
