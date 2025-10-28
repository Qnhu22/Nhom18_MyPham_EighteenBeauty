package com.oneshop.repository;

import com.oneshop.entity.Shipper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipperRepository extends JpaRepository<Shipper, Long> {

    // Tìm shipper theo userId (khi đăng nhập)
    Shipper findByUser_UserId(Long userId);
}

