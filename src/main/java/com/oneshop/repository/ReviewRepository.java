package com.oneshop.repository;

import com.oneshop.entity.Review;
import com.oneshop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductAndStatusTrueOrderByCreatedAtDesc(Product product);
}
