package com.oneshop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Dùng roleId sẵn trong DB
    private Long roleId;

    @Column(nullable = false, unique = true)
    private String roleName;   // VD: ROLE_USER, ROLE_ADMIN, ROLE_SELLER, ROLE_SHIPPER
}
