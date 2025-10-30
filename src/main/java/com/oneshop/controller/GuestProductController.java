package com.oneshop.controller;

import com.oneshop.entity.Category;
import com.oneshop.entity.Product;
import com.oneshop.entity.Blog;
import com.oneshop.service.CategoryService;
import com.oneshop.service.ProductService;
import com.oneshop.service.BlogService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class GuestProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BlogService blogService;

    // ✅ Constructor đầy đủ 3 service
    public GuestProductController(ProductService productService,
                                  CategoryService categoryService,
                                  BlogService blogService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.blogService = blogService;
    }

    // 🏠 Trang chủ
    @GetMapping({"/", "/index"})
    public String home(Model model) {

        // ⚙️ Dữ liệu gốc
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("bestSellers", productService.getBestSellerProducts());
        model.addAttribute("bestDeals", productService.getBestDeals());
        model.addAttribute("newProducts", productService.getNewProducts());
        model.addAttribute("highlightProducts", productService.getHighlightedProducts());
        model.addAttribute("guestProducts", productService.getTopProductsForGuest());
        model.addAttribute("pageTitle", "OneShop - Cửa hàng mỹ phẩm");

        // 🌸 Phần “By Categories” — Ảnh tĩnh demo
        List<Map<String, String>> featuredCategories = List.of(
                Map.of("name", "Kem chống nắng", "image", "/images/categories/kemchongnang.jpeg"),
                Map.of("name", "Kem nền", "image", "/images/categories/kemnen.jpeg"),
                Map.of("name", "Phấn phủ", "image", "/images/categories/phanphu.jpeg"),
                Map.of("name", "Son môi", "image", "/images/categories/sonmoi.jpeg")
        );
        model.addAttribute("featuredCategories", featuredCategories);

        // 📰 From The Blog — Lấy 3 bài mới nhất
        List<Blog> blogs = blogService.getLatestBlogs(3);
        model.addAttribute("blogs", blogs);

        return "index";
    }

    // 📦 Xem sản phẩm theo danh mục (có phân trang)
    @GetMapping("/category/{id}")
    public String productsByCategory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        // 🔹 Lấy sản phẩm theo trang
        Page<Product> productPage = productService.getAllProducts(page, size);

        // 🔹 Lọc theo danh mục
        List<Product> products = productPage.getContent().stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getCategoryId().equals(id))
                .toList();

        model.addAttribute("category", category);
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("pageTitle", "Danh mục: " + category.getName());
        return "category";
    }

    // 🔍 Chi tiết sản phẩm
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("pageTitle", product.getName());
        return "product-detail";
    }
}
