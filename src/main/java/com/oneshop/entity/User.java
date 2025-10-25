package com.oneshop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username"}),
        @UniqueConstraint(columnNames = {"email"}),
        @UniqueConstraint(columnNames = {"phone"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password; // Đã mã hóa bằng BCrypt

    @Column(length = 100)
    private String fullName;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 10)
    private String gender; // Nam / Nữ / Khác

    @Column(length = 255)
    private String avatar; // Ảnh đại diện

    @Column(nullable = false)
    private boolean active = false; // Mặc định false (chưa kích hoạt OTP)

    // 🔗 FK → order_addresses(addressId)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defaultAddressId")
    private OrderAddress defaultAddress;

    @CreationTimestamp
    private LocalDateTime createAt; // Thời điểm tạo tài khoản

    @UpdateTimestamp
    private LocalDateTime updateAt; // Thời điểm cập nhật gần nhất

    @Column(length = 10)
    private String otpCode;

    private LocalDateTime otpExpiry;

    @Column(length = 255)
    private String note; // Ghi chú nội bộ

    private LocalDateTime lastLogin; // Lần đăng nhập gần nhất

    // ================== QUAN HỆ ==================

    // 1️⃣ Danh sách vai trò (Admin, User, Shipper,...)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // 2️⃣ Danh sách địa chỉ giao hàng của user
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderAddress> addresses = new HashSet<>();
    
    @OneToOne(mappedBy = "manager")
    private Shop shop;

}
