package com.oneshop.service;

import com.oneshop.entity.Product;
import com.oneshop.entity.Review;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

public interface ReviewService {

    /**
     * Lấy danh sách review theo sản phẩm
     */
    List<Review> getReviewsByProduct(Product product);

    /**
     * Thêm review mới và tự động cập nhật rating trung bình của sản phẩm
     */
    void addReview(Long productId, String username, int rating, String comment);

    /**
     * Cập nhật lại điểm rating trung bình cho sản phẩm
     */
    void updateProductRating(Product product);
    
    Page<Review> filterReviews(Long productId, Boolean status, Integer rating,
            LocalDateTime fromDate, LocalDateTime toDate,
            String keyword, int page);
    Optional<Review> getById(Long reviewId);
    Review saveOrUpdate(Review review);
    void deleteById(Long reviewId);
}
