package com.oneshop.service.impl;

import com.oneshop.entity.Order;
import com.oneshop.entity.OrderItem;
import com.oneshop.entity.ProductVariant;
import com.oneshop.entity.User;
import com.oneshop.enums.OrderStatus;
import com.oneshop.repository.OrderRepository;
import com.oneshop.repository.ProductVariantRepository;
import com.oneshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    @Override
    public List<Order> getOrdersByUserAndStatus(User user, OrderStatus status) {
        return orderRepository.findByUserAndStatusOrderByOrderDateDesc(user, status);
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Order saveOrder(Order order) {
        System.out.println("📝 [DEBUG] Lưu order cho user: " +
                (order.getUser() != null ? order.getUser().getUsername() : "null"));
        Order saved = orderRepository.save(order);
        System.out.println("✅ [DEBUG] Đã lưu orderId = " + saved.getOrderId());
        return saved;
    }

    /**
     * Quy tắc thay đổi trạng thái + cập nhật soldCount:
     * - NEW, CONFIRMED → có thể huỷ (→ CANCELLED)
     * - DELIVERED → có thể trả hàng (→ RETURNED)
     * - SHIPPING, CANCELLED, RETURNED → không cho đổi
     * 
     * Khi chuyển sang DELIVERED → tăng soldCount
     * Khi chuyển sang RETURNED → giảm soldCount
     */
    @Override
    @Transactional
    public boolean changeStatus(Long orderId, User owner, OrderStatus toStatus, String note) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return false;
        if (!order.getUser().getUserId().equals(owner.getUserId())) return false; // không phải chủ đơn

        OrderStatus current = order.getStatus();
        boolean allowed = false;

        if (toStatus == OrderStatus.CANCELLED) {
            allowed = EnumSet.of(OrderStatus.NEW, OrderStatus.CONFIRMED).contains(current);
        } else if (toStatus == OrderStatus.RETURNED) {
            allowed = (current == OrderStatus.DELIVERED);
        } else if (toStatus == OrderStatus.DELIVERED) {
            allowed = (current == OrderStatus.SHIPPING || current == OrderStatus.CONFIRMED);
        }

        if (!allowed) return false;

        order.setStatus(toStatus);
        if (note != null && !note.isBlank()) {
            String old = order.getNote() == null ? "" : order.getNote() + " | ";
            order.setNote(old + note);
        }

     // ✅ Cập nhật soldCount và hoàn kho
        if (toStatus == OrderStatus.DELIVERED) {
            for (OrderItem item : order.getOrderItems()) {
                ProductVariant variant = item.getProductVariant();
                if (variant != null) {
                    int oldSold = Optional.ofNullable(variant.getSoldCount()).orElse(0);
                    variant.setSoldCount(oldSold + item.getQuantity());
                    productVariantRepository.save(variant);
                }
            }
        } else if (toStatus == OrderStatus.RETURNED || toStatus == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getOrderItems()) {
                ProductVariant variant = item.getProductVariant();
                if (variant != null) {
                    // ✅ Hoàn lại kho
                    int oldStock = Optional.ofNullable(variant.getStock()).orElse(0);
                    variant.setStock(oldStock + item.getQuantity());
                    productVariantRepository.save(variant);
                }
            }
        }


        orderRepository.save(order);
        return true;
    }
}
