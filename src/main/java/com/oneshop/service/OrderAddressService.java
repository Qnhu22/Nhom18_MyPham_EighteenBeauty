package com.oneshop.service;

import com.oneshop.entity.OrderAddress;
import com.oneshop.entity.User;

import java.util.List;

public interface OrderAddressService {
	List<OrderAddress> getAddressesByUser(User user);
    OrderAddress saveAddress(OrderAddress address, User user);
    void deleteAddress(Long id, User user);
    void setDefaultAddress(Long id, User user);
    OrderAddress getAddressById(Long id);
    List<OrderAddress> getAddressesByUser(Long userId);
    List<OrderAddress> getAllAddresses();
    OrderAddress getById(Long id);
    void setDefaultAddress(OrderAddress selected);
    void delete(Long id);
    void save(OrderAddress address);
}