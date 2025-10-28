package com.oneshop.service;

import com.oneshop.entity.Shipper;
import java.util.List;

public interface ShipperService {

    List<Shipper> getAllShippers();
    Shipper getShipperById(Long id);
    Shipper getShipperByUserId(Long userId);
    Shipper saveShipper(Shipper shipper);
    Shipper findByUsername(String username);
    void deleteShipper(Long id);
	void save(Shipper shipper);
}
