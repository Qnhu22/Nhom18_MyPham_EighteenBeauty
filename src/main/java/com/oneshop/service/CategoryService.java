package com.oneshop.service;

import com.oneshop.entity.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> getAllCategories();
    Optional<Category> getByName(String name);
    Optional<Category> getCategoryById(Long id);
    boolean existsByName(String name);
    Category createCategory(Category category);
    Category updateCategory(Long id, Category category);
    void toggleStatus(Long id);
    void deleteCategory(Long id);
    List<Category> searchCategories(String keyword);
    Category findByName(String name);
    Category findById(Long id);
}
