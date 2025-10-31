package com.oneshop.service.impl;

import com.oneshop.entity.Shipper;
import com.oneshop.entity.User;
import com.oneshop.repository.ShipperRepository;
import com.oneshop.service.ShipperService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
    
    @Override
    public Page<Shipper> getAllShippersWithPaging(Pageable pageable) {
        return shipperRepository.findAll(pageable);
    }

    @Override
    public Page<Shipper> searchShippersWithPaging(String keyword, Pageable pageable) {
        return shipperRepository.findByUserFullNameContaining(keyword, pageable);
    }
    
    @Override
    public Shipper getShipperByUserId(Long userId) {
        return shipperRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin shipper!"));
    }

}
