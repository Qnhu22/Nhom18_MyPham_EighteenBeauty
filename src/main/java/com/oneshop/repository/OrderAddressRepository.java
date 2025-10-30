package com.oneshop.repository;

import com.oneshop.entity.OrderAddress;
import com.oneshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderAddressRepository extends JpaRepository<OrderAddress, Long> {
    List<OrderAddress> findByUser(User user);
    Optional<OrderAddress> findFirstByUserAndIsDefaultTrue(User user);
    Optional<OrderAddress> findByAddressIdAndUser(Long addressId, User user);
}
