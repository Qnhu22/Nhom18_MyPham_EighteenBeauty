package com.oneshop.service;

import com.oneshop.entity.Shipper;
import com.oneshop.entity.User;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShipperService {

    List<Shipper> getAllShippers();

    Shipper getShipperById(Long id);

    Shipper saveShipper(Shipper shipper);

    void deleteShipper(Long id);

    List<Shipper> searchShippers(String keyword);

    void createIfAbsent(User user); // tạo shipper mặc định nếu chưa có (dành cho user có role SHIPPER)
    
    Page<Shipper> getAllShippersWithPaging(Pageable pageable);

	Page<Shipper> searchShippersWithPaging(String keyword, Pageable pageable);
	
	Shipper getShipperByUserId(Long userId);
}
