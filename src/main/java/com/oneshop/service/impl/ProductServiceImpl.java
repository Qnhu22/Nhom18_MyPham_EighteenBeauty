package com.oneshop.service.impl;

import com.oneshop.entity.Category;
import com.oneshop.entity.Product;
import com.oneshop.entity.ProductImage;
import com.oneshop.entity.ProductVariant;
import com.oneshop.repository.ProductRepository;
import com.oneshop.repository.ProductVariantRepository;
import com.oneshop.service.ProductService;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {
	private static final String UPLOAD_DIR = "uploads/products/";

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    /* =====================================================
       📦 Lấy thông tin sản phẩm
       ===================================================== */
    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm để xóa"));

        // Xóa variant trước (tránh lỗi khóa ngoại)
        List<ProductVariant> variants = variantRepository.findByProduct(product);
        variantRepository.deleteAll(variants);

        productRepository.delete(product);
    }

    /* =====================================================
       🏠 Feed trang chủ
       ===================================================== */
    /** 🏆 Sản phẩm nổi bật (Top bán chạy) */
    @Override
    public List<Product> getHighlightedProducts() {
        return productRepository.findBestSellingProducts();
    }

    /** 🔥 Top bán chạy */
    @Override
    public List<Product> getBestSellerProducts() {
        return productRepository.findBestSellingProducts();
    }

    /** 🆕 Sản phẩm mới nhất */
    @Override
    public List<Product> getNewProducts() {
        return productRepository.findTop8ByOrderByCreatedAtDesc();
    }

    /** 💰 Sản phẩm giá tốt (giảm giá hoặc variant có oldPrice > price) */
    @Override
    public List<Product> getBestDeals() {
        return productRepository.findAll().stream()
                .filter(p -> p.getVariants() != null && p.getVariants().stream()
                        .anyMatch(v -> v.getOldPrice() != null && v.getOldPrice().compareTo(v.getPrice()) > 0))
                .limit(8)
                .toList();
    }

    /** 👥 Feed cho khách (guest) */
    @Override
    public List<Product> getTopProductsForGuest() {
        return productRepository.findTopProductsForGuest();
    }

    /* =====================================================
       🔍 Tìm kiếm & phân trang
       ===================================================== */
    @Override
    public Page<Product> getProductsByName(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    @Override
    public Page<Product> getProductsByCategory(Category category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByCategory(category, pageable);
    }

    @Override
    public Page<Product> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> getProductsByStatus(Boolean status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByStatus(status, pageable);
    }

    /* =====================================================
       ⚙️ Quản lý sản phẩm
       ===================================================== */
    @Override
    public List<Product> getBestSellingProducts() {
        return productRepository.findBestSellingProducts();
    }

    @Override
    public Product saveProduct(Product product) {
        // 🧩 Lưu product
        Product savedProduct = productRepository.save(product);

        // 🧩 Lưu variant kèm theo (nếu có)
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            for (ProductVariant variant : product.getVariants()) {
                variant.setProduct(savedProduct);
                if (variant.getCreatedAt() == null) {
                    variant.setCreatedAt(LocalDateTime.now());
                }
                variantRepository.save(variant);
            }
        }

        return savedProduct;
    }

    @Override
    public boolean existsById(Long productId) {
        return productRepository.existsById(productId);
    }

    /** Lấy danh sách variant theo sản phẩm */
    @Override
    public List<ProductVariant> getVariantsByProduct(Product product) {
        return variantRepository.findByProduct(product);
    }

    /* =====================================================
       💡 Hỗ trợ hiển thị giá min–max
       ===================================================== */
    @Override
    public String getPriceRange(Product product) {
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            return "₫0";
        }

        double min = product.getVariants().stream()
                .filter(v -> v.getPrice() != null)
                .map(v -> v.getPrice().doubleValue())
                .min(Comparator.naturalOrder())
                .orElse(0.0);

        double max = product.getVariants().stream()
                .filter(v -> v.getPrice() != null)
                .map(v -> v.getPrice().doubleValue())
                .max(Comparator.naturalOrder())
                .orElse(min);

        if (min == max) {
            return String.format("₫%,.0f", min);
        } else {
            return String.format("₫%,.0f - ₫%,.0f", min, max);
        }
    }

	@Override
	public Product findById(Long productId) {
		return productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
	}

	@Override
	public Page<Product> filterProducts(String keyword, Long categoryId, Long brandId, String status, int page) {
		Specification<Product> spec = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			// 🔹 Lọc theo danh mục
			if (categoryId != null && categoryId > 0) {
				predicates.add(cb.equal(root.get("category").get("id"), categoryId));
			}

			// 🔹 Lọc theo thương hiệu
			if (brandId != null && brandId > 0) {
				predicates.add(cb.equal(root.get("brand").get("id"), brandId));
			}

			// 🔹 Lọc theo từ khóa (tên hoặc mô tả)
			if (keyword != null && !keyword.trim().isEmpty()) {
				String likePattern = "%" + keyword.trim().toLowerCase() + "%";
				Predicate nameMatch = cb.like(cb.lower(root.get("name")), likePattern);
				Predicate descMatch = cb.like(cb.lower(root.get("description")), likePattern);
				predicates.add(cb.or(nameMatch, descMatch));
			}

			// 🔹 Lọc theo trạng thái hoạt động
			if (status != null && !status.equalsIgnoreCase("all")) {
				boolean isActive = status.equalsIgnoreCase("active");
				predicates.add(cb.equal(root.get("status"), isActive));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};

		Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "productId"));
		return productRepository.findAll(spec, pageable);
	}

	@Transactional
	@Override
	public Product saveOrUpdateProduct(Product product, MultipartFile[] productImages, MultipartFile[] variantImages,
			Long[] removeImageIds) {
		boolean isNew = (product.getProductId() == null);
		Product targetProduct;
		if (isNew) {
			// Thêm mới
			targetProduct = product;
			targetProduct.setCreatedAt(LocalDateTime.now());
		} else {
			// Cập nhật
			targetProduct = productRepository.findById(product.getProductId())
					.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

			// Cập nhật các field cơ bản
			targetProduct.setName(product.getName());
			targetProduct.setDescription(product.getDescription());
			targetProduct.setCategory(product.getCategory());
			targetProduct.setBrand(product.getBrand());
			targetProduct.setStatus(product.getStatus());
		}

		List<ProductImage> images = targetProduct.getImages();
		if (images == null) {
			images = new ArrayList<>();
		}
		// 🧹 Xóa ảnh theo ID
		if (removeImageIds != null) {
			for (Long imgId : removeImageIds) {
				images.removeIf(img -> {
					if (img.getImageId().equals(imgId)) {
						File file = new File(UPLOAD_DIR + img.getImageUrl().replace("/uploads/products/", ""));
						if (file.exists())
							file.delete();
						return true;
					}
					return false;
				});
			}
		}

		// 📸 Thêm ảnh mới
		if (productImages != null) {
			for (MultipartFile file : productImages) {
				if (!file.isEmpty()) {
					try {
						String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
						Path filePath = Paths.get(UPLOAD_DIR, fileName);
						Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

						ProductImage newImg = new ProductImage();
						newImg.setImageUrl("/uploads/products/" + fileName);
						newImg.setProduct(targetProduct);
						images.add(newImg);
					} catch (IOException e) {
						throw new RuntimeException("Lỗi khi lưu ảnh mới: " + e.getMessage());
					}
				}
			}
		}

		targetProduct.setImages(images);

		List<ProductVariant> existingVariants = targetProduct.getVariants();
	    if (existingVariants == null) {
	        existingVariants = new ArrayList<>();
	        targetProduct.setVariants(existingVariants);
	    }

	    Map<Long, ProductVariant> existMap = existingVariants.stream()
	            .filter(v -> v.getVariantId() != null)
	            .collect(Collectors.toMap(ProductVariant::getVariantId, v -> v));

	    Set<Long> updatedIds = product.getVariants() == null ? Set.of() :
	            product.getVariants().stream()
	                    .map(ProductVariant::getVariantId)
	                    .filter(Objects::nonNull)
	                    .collect(Collectors.toSet());

	    // ✅ Xóa variants bị xóa trên UI
	    Iterator<ProductVariant> it = existingVariants.iterator();
	    while (it.hasNext()) {
	        ProductVariant old = it.next();
	        if (old.getVariantId() != null && !updatedIds.contains(old.getVariantId())) {
	            it.remove(); // orphanRemoval → Hibernate DELETE đúng variant
	        }
	    }

	    // ✅ Update hoặc thêm mới biến thể
	    if (product.getVariants() != null) {
	        List<ProductVariant> formVariants = new ArrayList<>(product.getVariants()); // tránh reference vòng

	        for (int i = 0; i < formVariants.size(); i++) {
	            ProductVariant newVar = formVariants.get(i);

	            // 🖼️ Xử lý ảnh biến thể (nếu có)
	            if (variantImages != null && i < variantImages.length && !variantImages[i].isEmpty()) {
	                try {
	                    String fileName = UUID.randomUUID() + "_" + variantImages[i].getOriginalFilename();
	                    Path path = Paths.get(UPLOAD_DIR, fileName);
	                    Files.copy(variantImages[i].getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
	                    newVar.setImageUrl("/uploads/products/" + fileName);
	                } catch (IOException e) {
	                    throw new RuntimeException("Lỗi upload ảnh biến thể: " + e.getMessage());
	                }
	            }

	            // 🧩 Nếu là variant cũ → cập nhật
	            if (newVar.getVariantId() != null && existMap.containsKey(newVar.getVariantId())) {
	                ProductVariant oldVar = existMap.get(newVar.getVariantId());
	                oldVar.setName(newVar.getName());
	                oldVar.setColor(newVar.getColor());
	                oldVar.setSize(newVar.getSize());
	                oldVar.setPrice(newVar.getPrice());
	                oldVar.setOldPrice(newVar.getOldPrice());
	                oldVar.setStock(newVar.getStock());
	                if (newVar.getImageUrl() != null) {
	                    oldVar.setImageUrl(newVar.getImageUrl());
	                }
	            } 
	            // 🧩 Nếu là variant mới → tạo mới hoàn toàn (tránh vòng lặp)
	            else {
	                ProductVariant variant = new ProductVariant();
	                variant.setProduct(targetProduct);
	                variant.setName(newVar.getName());
	                variant.setColor(newVar.getColor());
	                variant.setSize(newVar.getSize());
	                variant.setPrice(newVar.getPrice());
	                variant.setOldPrice(newVar.getOldPrice());
	                variant.setStock(newVar.getStock());
	                variant.setSoldCount(0);
	                variant.setImageUrl(newVar.getImageUrl());
	                existingVariants.add(variant);
	            }
	        }
	    }
	    //if (isNew) targetProduct.setCreatedAt(LocalDateTime.now());

		
		try {
		    Product saved = productRepository.save(targetProduct);
		    productRepository.flush();
		    return saved;
		} catch (Exception e) {
		    e.printStackTrace();
		    throw new RuntimeException("❌ Lỗi khi lưu: " + e.getMessage());
		}

	}
}
