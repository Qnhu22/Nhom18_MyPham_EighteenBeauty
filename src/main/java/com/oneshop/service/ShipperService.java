package com.oneshop.service;

import com.oneshop.entity.Shipper;
import com.oneshop.entity.User;

import java.util.List;

public interface ShipperService {

    List<Shipper> getAllShippers();

    Shipper getShipperById(Long id);

    Shipper saveShipper(Shipper shipper);

    void deleteShipper(Long id);

    List<Shipper> searchShippers(String keyword);

    void createIfAbsent(User user); // tạo shipper mặc định nếu chưa có (dành cho user có role SHIPPER)
}
