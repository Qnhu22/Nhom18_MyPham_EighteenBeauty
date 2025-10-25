package com.oneshop.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oneshop.entity.Product;
import com.oneshop.entity.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
	List<ProductVariant> findByProduct(Product product);
}
