package com.oneshop.repository;

import com.oneshop.entity.Shipper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipperRepository extends JpaRepository<Shipper, Long> {
	List<Shipper> findByAreaContainingIgnoreCaseOrStatusContainingIgnoreCase(String area, String status);
	Optional<Shipper> findByUser_UserId(Long userId);
	boolean existsByUser_UserId(Long userId);
}