package com.oneshop.controller;

import com.oneshop.entity.Category;
import com.oneshop.entity.Product;
import com.oneshop.service.CategoryService;
import com.oneshop.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class GuestProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public GuestProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    // 🏠 Trang chủ
    @GetMapping({"/", "/index"})
    public String home(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("bestSellers", productService.getBestSellerProducts());
        model.addAttribute("bestDeals", productService.getBestDeals());
        model.addAttribute("newProducts", productService.getNewProducts());
        model.addAttribute("highlightProducts", productService.getHighlightedProducts());
        model.addAttribute("guestProducts", productService.getTopProductsForGuest());
        model.addAttribute("pageTitle", "OneShop - Cửa hàng mỹ phẩm");
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
