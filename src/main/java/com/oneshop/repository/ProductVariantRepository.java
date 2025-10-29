package com.oneshop.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.oneshop.entity.Product;
import com.oneshop.entity.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
	List<ProductVariant> findByProduct(Product product);

	// Top selling variants by soldCount (simple, uses sold_count column)
	@Query(value = "SELECT * FROM product_variants ORDER BY soldCount DESC", nativeQuery = true)
	List<ProductVariant> findTopBySoldCount(Pageable pageable);
}
