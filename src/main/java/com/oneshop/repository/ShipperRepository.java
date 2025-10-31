package com.oneshop.repository;

import com.oneshop.entity.Shipper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShipperRepository extends JpaRepository<Shipper, Long> {
	List<Shipper> findByAreaContainingIgnoreCaseOrStatusContainingIgnoreCase(String area, String status);
	Optional<Shipper> findByUser_UserId(Long userId);
	boolean existsByUser_UserId(Long userId);
	@Query("SELECT s FROM Shipper s WHERE s.user.userId = :userId")
    Optional<Shipper> findByUserId(@Param("userId") Long userId);
	
	Page<Shipper> findByUserFullNameContaining(String fullName, Pageable pageable);
}

