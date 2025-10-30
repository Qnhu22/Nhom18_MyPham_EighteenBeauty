package com.oneshop.service.impl;

import com.oneshop.entity.Shipper;
import com.oneshop.entity.User;
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
    public Shipper saveShipper(Shipper shipper) {
        return shipperRepository.save(shipper);
    }

    @Override
    public void deleteShipper(Long id) {
        shipperRepository.deleteById(id);
    }

    @Override
    public List<Shipper> searchShippers(String keyword) {
        return shipperRepository.findByAreaContainingIgnoreCaseOrStatusContainingIgnoreCase(keyword, keyword);
    }

    @Override
    public void createIfAbsent(User user) {
        if (!shipperRepository.existsByUser_UserId(user.getUserId())) {
            Shipper newShipper = Shipper.builder()
                    .user(user)
                    .status("Đang hoạt động")
                    .area("Chưa phân vùng")
                    .build();
            shipperRepository.save(newShipper);
        }
    }
}
