package com.oneshop.repository;

import com.oneshop.entity.Order;
import com.oneshop.entity.User;
import com.oneshop.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);
    List<Order> findByUserAndStatusOrderByOrderDateDesc(User user, OrderStatus status);
}
