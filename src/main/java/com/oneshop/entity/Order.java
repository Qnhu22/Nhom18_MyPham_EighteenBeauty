package com.oneshop.entity;

import com.oneshop.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    // 🔹 Người đặt hàng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 🔹 Người giao hàng (nếu có)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id")
    private User shipper;

    // 🔹 Ngày đặt hàng (tự động tạo khi insert)
    @CreationTimestamp
    private LocalDateTime orderDate;

    // 🔹 Trạng thái đơn hàng
    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private OrderStatus status = OrderStatus.NEW;
    // Có thể là: NEW, CONFIRMED, SHIPPING, DELIVERED, CANCELLED, RETURNED,...

    // 🔹 Tổng tiền hàng (trước giảm)
    @Column(precision = 12, scale = 2)
    private BigDecimal totalAmount;

    // 🔹 Địa chỉ giao hàng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private OrderAddress address;

    // 🔹 Phương thức và trạng thái thanh toán
    @Column(length = 50)
    private String paymentMethod; // COD, BANKING, MOMO,...
    @Column(length = 50)
    private String paymentStatus; // UNPAID, PAID, REFUNDED,...

    // 🔹 Phí vận chuyển và tổng cuối cùng
    @Column(precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @Column(precision = 12, scale = 2)
    private BigDecimal finalAmount;

    // 🔹 Voucher (nếu có)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_product_id")
    private Voucher productVoucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_shipping_id")
    private Voucher shippingVoucher;

    // 🔹 Ghi chú thêm của khách hàng
    @Column(length = 255)
    private String note;

    // 🔹 Danh sách sản phẩm trong đơn
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;
}
