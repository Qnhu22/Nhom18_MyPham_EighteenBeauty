package com.oneshop.service;

import com.oneshop.entity.Product;
import com.oneshop.entity.Review;

import java.util.List;

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
}
