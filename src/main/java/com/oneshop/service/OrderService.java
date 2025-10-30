package com.oneshop.service;

import com.oneshop.entity.Order;
import com.oneshop.entity.User;
import com.oneshop.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    List<Order> getOrdersByUser(User user);
    List<Order> getOrdersByUserAndStatus(User user, OrderStatus status);
    Order getOrderById(Long id);
    Order saveOrder(Order order);
    boolean changeStatus(Long orderId, User owner, OrderStatus toStatus, String note);
}
