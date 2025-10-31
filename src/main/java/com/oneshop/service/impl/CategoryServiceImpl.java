package com.oneshop.service.impl;

import com.oneshop.entity.Category;
import com.oneshop.repository.CategoryRepository;
import com.oneshop.service.CategoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepository;

	@Override
	public List<Category> getAllCategories() {
		return categoryRepository.findAll();
	}

	@Override
	public List<Category> searchCategories(String keyword) {
		return categoryRepository.findByNameContainingIgnoreCase(keyword);
	}

	@Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

	@Override
	public Category createCategory(Category category) {
		return categoryRepository.save(category);
	}

	@Override
	public void deleteCategory(Long id) {
		categoryRepository.deleteById(id);
	}

	@Override
	public void toggleStatus(Long id) {
		Optional<Category> optional = categoryRepository.findById(id);
		optional.ifPresent(category -> {
			category.setStatus(!category.isStatus());
			categoryRepository.save(category);
		});
	}

	@Override
	public boolean existsByName(String name) {
		return categoryRepository.existsByName(name);
	}

	@Override
	public Category updateCategory(Long id, Category category) {
		return categoryRepository.findById(id).map(existing -> {
			existing.setName(category.getName());
			return categoryRepository.save(existing);
		}).orElseThrow(() -> new RuntimeException("Category not found"));
	}
	
	@Override
	public Category findById(Long id) {
		return categoryRepository.findById(id).orElse(null);
	}
	
	@Override
	public Page<Category> getAllCategories(Pageable pageable) {
	    return categoryRepository.findAll(pageable);
	}

	@Override
	public Page<Category> searchCategories(String keyword, Pageable pageable) {
	    return categoryRepository.findByNameContainingIgnoreCase(keyword, pageable);
	}

}
