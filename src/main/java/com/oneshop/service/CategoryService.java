package com.oneshop.service;

import com.oneshop.entity.Category;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    List<Category> getAllCategories();
    Optional<Category> getCategoryById(Long id);
    boolean existsByName(String name);
    Category createCategory(Category category);
    Category updateCategory(Long id, Category category);
    void toggleStatus(Long id);
    void deleteCategory(Long id);
    List<Category> searchCategories(String keyword);
	Category findById(Long categoryId);
	
	Page<Category> getAllCategories(Pageable pageable);
    Page<Category> searchCategories(String keyword, Pageable pageable);
}
