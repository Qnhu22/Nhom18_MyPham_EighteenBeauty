package com.oneshop.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
//import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long variantId;

	@ManyToOne
	@JoinColumn(name = "productId", nullable = false)
	private Product product;

	@Column(columnDefinition = "nvarchar(255)")
	private String name; // ex: "Hồng đất 3.5g"

	@Column(precision = 12, scale = 2, nullable = false)
	private BigDecimal price;

	@Column(precision = 12, scale = 2)
	private BigDecimal oldPrice;

	@Column(nullable = false)
	private Integer stock;

	private Integer soldCount;

	@Column(length = 255)
	private String imageUrl;
	
	// tùy chọn, không bắt buộc
	@Column(columnDefinition = "nvarchar(100)")
	private String color;

	// tùy chọn, không bắt buộc
	@Column(columnDefinition = "nvarchar(100)")
	private String size;

	private LocalDateTime createdAt;
	
	@OneToMany(mappedBy = "variant")
	private Set<WishlistItem> wishlistItems;

}
