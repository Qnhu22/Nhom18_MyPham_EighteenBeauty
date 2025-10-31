package com.oneshop.repository;

import com.oneshop.entity.Category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    List<Category> findByNameContainingIgnoreCase(String keyword);
	boolean existsByName(String name);
	
	Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
