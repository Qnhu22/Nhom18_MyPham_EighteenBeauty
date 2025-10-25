package com.oneshop.controller;

import com.oneshop.entity.Category;
import com.oneshop.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // 📌 Danh sách + tìm kiếm
    @GetMapping
    public String listCategories(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<Category> categories;
        if (keyword != null && !keyword.isEmpty()) {
            categories = categoryService.searchCategories(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            categories = categoryService.getAllCategories();
        }
        model.addAttribute("categories", categories);
        model.addAttribute("pageTitle", "Quản lý Danh mục");
        return "admin/category-list";
    }

    // 📌 Hiển thị form tạo
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("pageTitle", "Tạo danh mục");
        return "admin/category-create";
    }

    // 📌 Xử lý tạo
    @PostMapping("/create")
    public String createCategory(@ModelAttribute("category") Category category,
                                 @RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (!file.isEmpty()) {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get("uploads/categories"); // thư mục ngoài project
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

                // Lưu đường dẫn tương đối vào DB
                category.setImageUrl("/uploads/categories/" + fileName);
            }
            categoryService.createCategory(category);
            redirectAttributes.addFlashAttribute("success", "✅ Tạo danh mục thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "❌ Lỗi khi upload ảnh: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // 📌 Hiển thị form update
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new RuntimeException("Category không tồn tại"));
        model.addAttribute("category", category);
        model.addAttribute("pageTitle", "Cập nhật danh mục");
        return "admin/category-edit";
    }

    // 📌 Xử lý update
    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @ModelAttribute("category") Category updatedCategory,
                                 @RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirectAttributes) {
        try {
            Category oldCategory = categoryService.getCategoryById(id)
                    .orElseThrow(() -> new RuntimeException("Category không tồn tại"));

            // cập nhật các field
            oldCategory.setName(updatedCategory.getName());
            oldCategory.setDescription(updatedCategory.getDescription());
            oldCategory.setStatus(updatedCategory.isStatus()); // ⚡ dùng isStatus() vì boolean

            // Nếu upload file mới
            if (!file.isEmpty()) {
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get("uploads/categories");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

                oldCategory.setImageUrl("/uploads/categories/" + fileName);
            }

            categoryService.updateCategory(id, oldCategory);
            redirectAttributes.addFlashAttribute("success", "✏️ Cập nhật danh mục thành công!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "❌ Lỗi khi upload ảnh: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // 📌 Xóa
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryService.deleteCategory(id);
        redirectAttributes.addFlashAttribute("success", "🗑️ Xóa danh mục thành công!");
        return "redirect:/admin/categories";
    }
}
