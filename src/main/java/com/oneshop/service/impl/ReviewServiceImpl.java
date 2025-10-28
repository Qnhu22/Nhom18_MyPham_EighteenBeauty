package com.oneshop.service.impl;

import com.oneshop.entity.Product;
import com.oneshop.entity.Review;
import com.oneshop.entity.User;
import com.oneshop.repository.ProductRepository;
import com.oneshop.repository.ReviewRepository;
import com.oneshop.repository.UserRepository;
import com.oneshop.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public List<Review> getReviewsByProduct(Product product) {
        return reviewRepository.findByProductAndStatusTrueOrderByCreatedAtDesc(product);
    }

    @Override
    @Transactional
    public void addReview(Long productId, String username, int rating, String comment) {
        // 🧍 Lấy user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user: " + username));

        // 📦 Lấy product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm ID: " + productId));

        // ✍️ Tạo review mới
        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(rating)
                .comment(comment)
                .status(true)
                .build();

        reviewRepository.save(review);

        // 🧮 Cập nhật lại điểm trung bình
        updateProductRating(product);
    }

    @Override
    @Transactional
    public void updateProductRating(Product product) {
        Double avg = reviewRepository.findByProductAndStatusTrueOrderByCreatedAtDesc(product)
                .stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        if (avg == 0.0) {
            product.setRating(null);
        } else {
            // Làm tròn 1 chữ số thập phân (ví dụ 4.5)
            float rounded = BigDecimal.valueOf(avg)
                    .setScale(1, RoundingMode.HALF_UP)
                    .floatValue();
            product.setRating(rounded);
        }

        productRepository.save(product);
    }
}
