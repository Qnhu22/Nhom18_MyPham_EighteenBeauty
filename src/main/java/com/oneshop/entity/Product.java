package com.oneshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 2000)
    private String description;

    // 🔗 Thương hiệu
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", referencedColumnName = "brandId")
    private Brand brand;

    // 🔗 Danh mục (FK → categories.categoryId)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "categoryId")
    private Category category;

    // ✅ Giữ thêm ID để truy xuất nhanh (read-only)
    @Column(name = "category_id", insertable = false, updatable = false)
    private Long categoryId;

    private Float rating;

    @Column(nullable = false)
    private Boolean status = true;

    @Column(length = 255)
    private String imageUrl;

    private LocalDateTime createdAt;

    // 🔗 Biến thể (size, màu,…)
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants;

    // 🔗 Ảnh phụ
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images;
    
 // 🔗 Khuyến mãi áp dụng cho sản phẩm này
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", referencedColumnName = "promotionId")
    private Promotion promotion;

}
