package com.oneshop.service.impl;

import com.oneshop.entity.Category;
import com.oneshop.repository.CategoryRepository;
import com.oneshop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    // ✅ Lấy tất cả danh mục
    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // ✅ Lấy danh mục theo ID
    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    // ✅ Lấy danh mục theo tên
    @Override
    public Optional<Category> getByName(String name) {
        return categoryRepository.findByName(name);
    }

    // ✅ Kiểm tra tồn tại theo tên
    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    // ✅ Tạo mới danh mục
    @Override
    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    // ✅ Cập nhật danh mục
    @Override
    public Category updateCategory(Long id, Category category) {
        return categoryRepository.findById(id).map(existing -> {
            existing.setName(category.getName());
            existing.setStatus(category.isStatus());
            return categoryRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Category not found"));
    }

    // ✅ Chuyển trạng thái hoạt động / ngưng
    @Override
    public void toggleStatus(Long id) {
        categoryRepository.findById(id).ifPresent(category -> {
            category.setStatus(!category.isStatus());
            categoryRepository.save(category);
        });
    }

    // ✅ Xóa danh mục
    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    // ✅ Tìm kiếm danh mục theo keyword
    @Override
    public List<Category> searchCategories(String keyword) {
        return categoryRepository.findByNameContainingIgnoreCase(keyword);
    }

    // ✅ Tìm danh mục theo tên (hàm findByName riêng biệt)
    @Override
    public Category findByName(String name) {
        return categoryRepository.findByName(name).orElse(null);
    }
}
