package com.oneshop.service.impl;

import com.oneshop.entity.Shipper;
import com.oneshop.repository.ShipperRepository;
import com.oneshop.service.ShipperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipperServiceImpl implements ShipperService {

    private final ShipperRepository shipperRepository;
    
    @Override
    public List<Shipper> getAllShippers() {
        return shipperRepository.findAll();
    }

    @Override
    public Shipper getShipperById(Long id) {
        return shipperRepository.findById(id).orElse(null);
    }

    @Override
    public Shipper getShipperByUserId(Long userId) {
        return shipperRepository.findByUser_UserId(userId);
    }

    @Override
    public Shipper saveShipper(Shipper shipper) {
        return shipperRepository.save(shipper);
    }

    @Override
    public void deleteShipper(Long id) {
        shipperRepository.deleteById(id);
    }

	@Override
	public Shipper findByUsername(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	public void save(Shipper shipper) {
        shipperRepository.save(shipper);
    }

	
}
