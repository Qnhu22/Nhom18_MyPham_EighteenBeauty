package com.oneshop.controller;

import com.oneshop.entity.*;
import com.oneshop.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ShopController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;

    // =============== SHOP PAGE ===============
    @GetMapping("/shop")
    public String shop(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        List<Category> categories = categoryService.getAllCategories();

        Page<Product> productPage;
        if (category != null && !category.isEmpty()) {
            Category selectedCategory = categories.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(category))
                    .findFirst()
                    .orElse(null);

            productPage = (selectedCategory != null)
                    ? productService.getProductsByCategory(selectedCategory, page, size)
                    : productService.getAllProducts(page, size);
        } else {
            productPage = productService.getAllProducts(page, size);
        }

        List<Product> products = productPage.getContent();

        if (products.isEmpty()) {
            products = List.of(
                    Product.builder().name("Kem chống nắng demo").imageUrl("/images/demo/product1.jpg").build(),
                    Product.builder().name("Son môi demo").imageUrl("/images/demo/product2.jpg").build(),
                    Product.builder().name("Phấn phủ demo").imageUrl("/images/demo/product3.jpg").build()
            );
        }

        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("products", products);
        model.addAttribute("pageTitle", category != null ? category : "Cửa hàng");

        return "shop";
    }

    // =============== PRODUCT DETAIL PAGE ===============
    @GetMapping("/shop/product/{id}")
    public String productDetail(@PathVariable("id") Long productId, Model model) {

        // 🟢 Lấy sản phẩm
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));

        // 🟢 Lấy biến thể (nếu có)
        List<ProductVariant> variants = product.getVariants();
        String mainImage = (variants != null && !variants.isEmpty())
                ? variants.get(0).getImageUrl()
                : product.getImageUrl();

        // 💰 Tính giá min-max
        double minPrice = 0, maxPrice = 0;
        if (variants != null && !variants.isEmpty()) {
            minPrice = variants.stream()
                    .filter(v -> v.getPrice() != null)
                    .mapToDouble(v -> v.getPrice().doubleValue())
                    .min().orElse(0);

            maxPrice = variants.stream()
                    .filter(v -> v.getPrice() != null)
                    .mapToDouble(v -> v.getPrice().doubleValue())
                    .max().orElse(minPrice);
        }

        // 📝 Lấy danh sách đánh giá
        List<Review> reviews = reviewService.getReviewsByProduct(product);

        // ⭐ Tính tổng lượt đánh giá và điểm trung bình
        double avgRating = reviews.isEmpty() ? 0 :
                reviews.stream().mapToInt(Review::getRating).average().orElse(0);

        // 📊 Tính phần trăm từng mức sao (1–5)
        Map<Integer, Long> countMap = reviews.stream()
                .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));

        Map<Integer, Integer> reviewPercent = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) {
            long count = countMap.getOrDefault(i, 0L);
            int percent = (reviews.isEmpty()) ? 0 : (int) Math.round((count * 100.0) / reviews.size());
            reviewPercent.put(i, percent);
        }

        // 🧮 Số lượng bán và tồn (nếu có variant)
        int soldCount = 0;
        int stockLeft = 0;
        if (variants != null && !variants.isEmpty()) {
            soldCount = variants.stream()
                    .mapToInt(v -> v.getSoldCount() != null ? v.getSoldCount() : 0)
                    .sum();
            stockLeft = variants.stream()
                    .mapToInt(v -> v.getStock() != null ? v.getStock() : 0)
                    .sum();
        }

        // 🏷️ Gửi dữ liệu sang view
        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("mainImage", mainImage);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewCount", reviews.size());
        model.addAttribute("avgRating", Math.round(avgRating * 10.0) / 10.0); // làm tròn 1 chữ số
        model.addAttribute("reviewPercent", reviewPercent);
        model.addAttribute("soldCount", soldCount);
        model.addAttribute("stockLeft", stockLeft);
        model.addAttribute("pageTitle", product.getName());

        return "product-detail";
    }

    // =============== ADD REVIEW ===============
    @PostMapping("/shop/product/{id}/review")
    public String addReview(@PathVariable("id") Long productId,
                            @RequestParam("rating") int rating,
                            @RequestParam("comment") String comment,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {

        // Nếu chưa đăng nhập → bắt đăng nhập
        if (principal == null) {
            return "redirect:/auth/login";
        }

        try {
            reviewService.addReview(productId, principal.getName(), rating, comment);
            redirectAttributes.addFlashAttribute("success", "Đánh giá của bạn đã được gửi!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi gửi đánh giá: " + e.getMessage());
        }

        return "redirect:/shop/product/" + productId;
    }
}
