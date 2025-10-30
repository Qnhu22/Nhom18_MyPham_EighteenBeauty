package com.oneshop.service.impl;

import com.oneshop.entity.Product;
import com.oneshop.entity.Review;
import com.oneshop.entity.User;
import com.oneshop.repository.ProductRepository;
import com.oneshop.repository.ReviewRepository;
import com.oneshop.repository.UserRepository;
import com.oneshop.service.ReviewService;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        // 🧮 Sau khi lưu review, cập nhật điểm trung bình
        updateProductRating(product);
    }

    @Override
    @Transactional
    public void updateProductRating(Product product) {
        // ⚡ Gọi query trung bình từ repository để nhanh hơn
        Double avg = reviewRepository.findAverageRatingByProduct(product);

        if (avg == null || avg == 0.0) {
            product.setRating(null);
        } else {
            // Làm tròn 1 chữ số thập phân (VD: 4.5)
            float rounded = BigDecimal.valueOf(avg)
                    .setScale(1, RoundingMode.HALF_UP)
                    .floatValue();
            product.setRating(rounded);
        }

        productRepository.save(product);
    }
    
    @Override
	public Page<Review> filterReviews(Long productId, Boolean status, Integer rating, LocalDateTime fromDate,
			LocalDateTime toDate, String keyword, int page) {
		Pageable pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<Review> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (productId != null) {
                predicates.add(cb.equal(root.get("product").get("id"), productId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (rating != null) {
                predicates.add(cb.equal(root.get("rating"), rating));
            }

            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }

            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDate));
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("comment")), kw),
                        cb.like(cb.lower(root.get("user").get("name")), kw),
                        cb.like(cb.lower(root.get("user").get("email")), kw)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return reviewRepository.findAll(spec, pageable);
	}

	@Override
	public Optional<Review> getById(Long reviewId) {
		return reviewRepository.findById(reviewId);
	}

	@Override
	public Review saveOrUpdate(Review review) {
		return reviewRepository.save(review);
	}

	@Override
	public void deleteById(Long reviewId) {
		reviewRepository.deleteById(reviewId);
	
	}
}
