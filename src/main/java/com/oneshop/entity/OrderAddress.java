package com.oneshop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.StringJoiner;

@Entity
@Table(name = "order_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "nvarchar(100)")
    private String receiverName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false, columnDefinition = "nvarchar(255)")
    private String addressLine;

    @Column(columnDefinition = "nvarchar(100)")
    private String ward;

    @Column(columnDefinition = "nvarchar(100)")
    private String district;

    @Column(columnDefinition = "nvarchar(100)")
    private String city;

    @Column(name = "isDefault", nullable = false)
    private boolean defaultAddress;
   
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Transient
    public String getFullAddress() {
        StringJoiner sj = new StringJoiner(", ");

        if (addressLine != null && !addressLine.isBlank()) sj.add(addressLine.trim());
        if (ward != null && !ward.isBlank()) sj.add(ward.trim());
        if (district != null && !district.isBlank()) sj.add(district.trim());
        if (city != null && !city.isBlank()) sj.add(city.trim());

        return sj.length() == 0 ? "—" : sj.toString();
    }
    
    @OneToMany(mappedBy = "address", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;
}

