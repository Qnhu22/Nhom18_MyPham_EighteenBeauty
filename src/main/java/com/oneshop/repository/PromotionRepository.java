package com.oneshop.repository;

import com.oneshop.entity.Promotion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
	List<Promotion> findByStatus(Boolean status);

	List<Promotion> findByNameContainingIgnoreCaseOrTypeContaining(String keyword, String keyword2);
}
