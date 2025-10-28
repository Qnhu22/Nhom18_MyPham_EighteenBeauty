package com.oneshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(
    name = "cart_items",
    uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "variant_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;

    // 🔗 Mỗi item thuộc về 1 giỏ hàng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    // 🔗 Mỗi item liên kết với 1 biến thể sản phẩm
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(nullable = false)
    private int quantity;

    // Giá tại thời điểm thêm vào giỏ
    @Column(precision = 12, scale = 2)
    private BigDecimal priceAtAdd;

    // ✅ Tính tổng tiền từng item (không lưu DB)
    @Transient
    public BigDecimal getSubtotal() {
        BigDecimal price = (priceAtAdd != null)
                ? priceAtAdd
                : productVariant.getPrice();
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
