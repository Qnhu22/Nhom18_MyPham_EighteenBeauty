package com.oneshop.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.oneshop.entity.*;
import com.oneshop.service.*;

//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;

@Controller
@RequestMapping("/manager/products")
public class ManagerProductController {

	@Autowired
	private ProductService productService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private BrandService brandService;

	@GetMapping
	public String listProducts(@RequestParam(required = false) Long categoryId,
			@RequestParam(required = false) Long brandId, @RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "all") String status, @RequestParam(defaultValue = "0") int page,
			Model model) {

		Page<Product> productPage = productService.filterProducts(keyword, categoryId, brandId, status, page);

		model.addAttribute("products", productPage.getContent());
		model.addAttribute("totalPages", productPage.getTotalPages());
		model.addAttribute("currentPage", page);
		model.addAttribute("status", status);
		model.addAttribute("keyword", keyword);
		model.addAttribute("categoryId", categoryId);
		model.addAttribute("brandId", brandId);

		model.addAttribute("categories", categoryService.getAllCategories());
		model.addAttribute("brands", brandService.findAll());

		return "manager/product/product-list";
	}

	// ===================== FORM THÊM / SỬA =====================
	@GetMapping({ "/add", "/edit/{id}" })
	public String productForm(@PathVariable(required = false) Long id, Model model) {
		Product product;

		if (id != null) {
			product = productService.getProductById(id)
					.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
			model.addAttribute("formTitle", "Cập nhật sản phẩm");
			model.addAttribute("existingImages", product.getImages());
		} else {
			product = new Product();
			model.addAttribute("formTitle", "Thêm sản phẩm mới");
		}

		model.addAttribute("product", product);
		model.addAttribute("categories", categoryService.getAllCategories());
		model.addAttribute("brands", brandService.findAll());
		model.addAttribute("actionUrl", "/manager/products/save");

		return "manager/product/product-form";
	}

	// ===================== LƯU (THÊM / SỬA) =====================
	@PostMapping("/save")
	public String saveProduct(@ModelAttribute Product product, @RequestParam Long categoryId,
			@RequestParam Long brandId,
			@RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles,
			@RequestParam(value = "variantImages", required = false) MultipartFile[] variantImages,
			@RequestParam(value = "removeImageIds", required = false) Long[] removeImageIds,
			RedirectAttributes redirectAttributes) {

		try {

			boolean isNew = (product.getProductId() == null);
			// Gán quan hệ Category & Brand
			product.setCategory(categoryService.findById(categoryId));
			product.setBrand(brandService.findById(brandId));

			productService.saveOrUpdateProduct(product, imageFiles, variantImages, removeImageIds);
			String msg = isNew ? "Thêm sản phẩm thành công!" : "Cập nhật sản phẩm thành công!";
			redirectAttributes.addFlashAttribute("success", msg);
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu sản phẩm: " + e.getMessage());
		}

		return "redirect:/manager/products";
	}

	// ===================== XÓA =====================
	@GetMapping("/delete/{id}")
	public String deleteProduct(@PathVariable Long id, RedirectAttributes redirect) {
		try {
			productService.deleteProduct(id);
			redirect.addFlashAttribute("success", "Xóa sản phẩm thành công!");
		} catch (Exception e) {
			redirect.addFlashAttribute("error", "Không thể xóa sản phẩm: " + e.getMessage());
		}
		return "redirect:/manager/products";
	}
	
	@GetMapping("/details/{id}")
	@ResponseBody
	public ResponseEntity<?> getProductDetail(@PathVariable Long id) {
	    try {
	        Product product = productService.findById(id);
	        if (product == null) return ResponseEntity.notFound().build();

	        Map<String, Object> response = new HashMap<>();
	        response.put("id", product.getProductId());
	        response.put("name", product.getName());
	        response.put("description", product.getDescription());
	        response.put("status", product.getStatus());
	        response.put("category", product.getCategory().getName());
	        response.put("brand", product.getBrand().getName());

	        // Ảnh sản phẩm
	        List<String> imageUrls = product.getImages()
	            .stream()
	            .map(ProductImage::getImageUrl)
	            .toList();
	        response.put("images", imageUrls);

	        // Biến thể
	        List<Map<String, Object>> variants = new ArrayList<>();
	        for (ProductVariant v : product.getVariants()) {
	            Map<String, Object> map = new HashMap<>();
	            map.put("id", v.getVariantId());
	            map.put("name", v.getName());
	            map.put("price", v.getPrice());
	            map.put("oldPrice", v.getOldPrice());
	            map.put("stock", v.getStock());
	            map.put("soldCount", v.getSoldCount());
	            map.put("imageUrl", v.getImageUrl());
	            variants.add(map);
	        }
	        response.put("variants", variants);

	        return ResponseEntity.ok(response);
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Lỗi khi lấy chi tiết sản phẩm: " + e.getMessage());
	    }
	}


}
