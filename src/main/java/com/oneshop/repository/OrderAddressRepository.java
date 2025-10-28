package com.oneshop.repository;

import com.oneshop.entity.OrderAddress;
import com.oneshop.entity.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderAddressRepository extends JpaRepository<OrderAddress, Long> {

    List<OrderAddress> findByUser(User user);
    OrderAddress findByUserAndIsDefaultTrue(User user);
    Optional<OrderAddress> findByAddressIdAndUser(Long addressId, User user);
}
