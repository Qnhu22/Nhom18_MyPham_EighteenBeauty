package com.oneshop.service.impl;

import com.oneshop.entity.OrderAddress;
import com.oneshop.entity.User;
import com.oneshop.repository.OrderAddressRepository;
import com.oneshop.service.OrderAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderAddressServiceImpl implements OrderAddressService {

    private final OrderAddressRepository addressRepo;

    @Override
    public List<OrderAddress> getAddressesByUser(User user) {
        return addressRepo.findByUser(user);
    }

    @Override
    @Transactional
    public OrderAddress saveAddress(OrderAddress address, User user) {
        address.setUser(user);

        // ✅ Nếu có ID → cập nhật địa chỉ cũ
        if (address.getAddressId() != null) {
            OrderAddress existing = addressRepo.findById(address.getAddressId())
                    .filter(a -> a.getUser().getUserId().equals(user.getUserId()))
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ để cập nhật"));

            existing.setReceiverName(address.getReceiverName());
            existing.setPhone(address.getPhone());
            existing.setAddressLine(address.getAddressLine());
            existing.setWard(address.getWard());
            existing.setDistrict(address.getDistrict());
            existing.setCity(address.getCity());

            // ✅ Nếu tick “Đặt làm mặc định”
            if (Boolean.TRUE.equals(address.isDefault())) {
                removeDefaultFromOther(user); // reset các địa chỉ khác
                existing.setDefault(true);
            } 
            // ❗ Nếu không tick, giữ nguyên trạng thái mặc định cũ
            // Không cần set false, vì người dùng không thay đổi

            return addressRepo.save(existing);
        }

        // ✅ Nếu là địa chỉ mới
        if (Boolean.TRUE.equals(address.isDefault())) {
            removeDefaultFromOther(user);
        }
        return addressRepo.save(address);
    }

    /**
     * 🧹 Bỏ mặc định các địa chỉ khác của user
     */
    private void removeDefaultFromOther(User user) {
        List<OrderAddress> list = addressRepo.findByUser(user);
        for (OrderAddress a : list) {
            a.setDefault(false);
        }
        addressRepo.saveAll(list);
    }

    @Override
    @Transactional
    public void deleteAddress(Long id, User user) {
        OrderAddress address = addressRepo.findById(id)
                .filter(a -> a.getUser().getUserId().equals(user.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ"));

        addressRepo.delete(address);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long id, User user) {
        // ✅ Bỏ mặc định các địa chỉ khác
        removeDefaultFromOther(user);

        // ✅ Đặt địa chỉ hiện tại làm mặc định
        OrderAddress selected = addressRepo.findById(id)
                .filter(a -> a.getUser().getUserId().equals(user.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ để đặt mặc định"));
        selected.setDefault(true);

        addressRepo.save(selected);
    }

    @Override
    public OrderAddress getAddressById(Long id) {
        return addressRepo.findById(id).orElse(null);
    }
}
