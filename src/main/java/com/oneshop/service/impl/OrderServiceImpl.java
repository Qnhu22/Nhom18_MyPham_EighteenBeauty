package com.oneshop.service.impl;

import com.oneshop.dto.ChartData;
import com.oneshop.dto.PerformanceStats;
import com.oneshop.entity.Order;
import com.oneshop.entity.OrderItem;
import com.oneshop.entity.OrderStatusHistory;
import com.oneshop.entity.Shipper;
import com.oneshop.entity.ProductVariant;
import com.oneshop.entity.User;
import com.oneshop.enums.OrderStatus;
import com.oneshop.repository.OrderItemRepository;
import com.oneshop.repository.OrderRepository;
import com.oneshop.repository.ProductVariantRepository;
import com.oneshop.repository.OrderStatusHistoryRepository;
import com.oneshop.repository.ShipperRepository;
import com.oneshop.service.OrderService;
import lombok.RequiredArgsConstructor;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import java.util.EnumSet;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    //private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private OrderStatusHistoryRepository historyRepository;
	@Autowired
	private OrderItemRepository orderItemRepository;
	@Autowired
	private ShipperRepository shipperRepository;

	@Override
	public Order save(Order order) {
		return orderRepository.save(order);
	}

    @Override
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }
	@Override
	public List<Order> getOrdersByShipperUserId(Long shipperId) {
		return orderRepository.findByShipper_ShipperId(shipperId);
	}

    @Override
    public List<Order> getOrdersByUserAndStatus(User user, OrderStatus status) {
        return orderRepository.findByUserAndStatusOrderByOrderDateDesc(user, status);
    }
	@Override
	public List<Order> getOrdersByShipperUserIdAndStatus(Long shipperId, String status) {
		OrderStatus orderStatus = OrderStatus.valueOf(status);
		return orderRepository.findByShipper_ShipperIdAndStatus(shipperId, orderStatus);
	}

	@Override
	public Order getOrderByIdAndShipperId(Long orderId, Long shipperId) {
		return orderRepository.findByOrderIdAndShipper_ShipperId(orderId, shipperId).orElse(null);
	}

	@Override
	public Order getOrderById(Long orderId) {
		return orderRepository.findById(orderId).orElse(null);
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
	@Override
	@Transactional
	public void updateOrderStatusByShipper(Long orderId, Long shipperId, String newStatus, String note) {
		Optional<Order> optionalOrder = orderRepository.findByOrderIdAndShipper_ShipperId(orderId, shipperId);
		if (!optionalOrder.isPresent())
			return;

		Order order = optionalOrder.get();
		OrderStatus oldStatus = order.getStatus(); // lưu trạng thái cũ

		// 1️⃣ Cập nhật trạng thái enum
		OrderStatus newStatusEnum = OrderStatus.valueOf(newStatus);
		order.setStatus(newStatusEnum);

		// 2️⃣ Nếu chuyển sang DELIVERED mà paymentStatus = "UNPAID" thì chuyển thành
		// "PAID"
		if (newStatusEnum == OrderStatus.DELIVERED && "UNPAID".equals(order.getPaymentStatus())) {
			order.setPaymentStatus("PAID");
		}

		orderRepository.save(order);

		// 3️⃣ Cập nhật lịch sử trong order_status_history (lưu String để tiện đọc)
		OrderStatusHistory history = OrderStatusHistory.builder().order(order).changedBy(null) // shipper entity nếu có
				.oldStatus(oldStatus.name()) // lưu String
				.newStatus(newStatusEnum.name()).note(note).changeAt(java.time.LocalDateTime.now()).build();
		historyRepository.save(history);
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
        }if (!allowed) return false;

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

	@Override
	public long countDeliveredByShipper(Long shipperId) {
		return orderRepository.countByShipper_ShipperIdAndStatus(shipperId, OrderStatus.DELIVERED);
	}

	@Override
	public long countFailedByShipper(Long shipperId) {
		return orderRepository.countByShipper_ShipperIdAndStatus(shipperId, OrderStatus.CANCELLED)
				+ orderRepository.countByShipper_ShipperIdAndStatus(shipperId, OrderStatus.RETURNED);
	}

	@Override
	public BigDecimal calculateTotalRevenueByShipper(Long shipperId) {
		return orderRepository.sumTotalDeliveredAmountByShipper(shipperId);
	}

	@Override
	public long countOrdersByShipper(Long shipperId) {
		return orderRepository.countByShipper_ShipperId(shipperId);
	}

	@Override
	public long countTotalOrdersByShipper(Long shipperId) {
		return orderRepository.countByShipper_ShipperId(shipperId);
	}

	@Override
	public List<ChartData> getMonthlyDeliveredStats(Long shipperId) {
		List<Object[]> results = orderRepository.getMonthlyDeliveredStats(shipperId);
		List<ChartData> chartData = new ArrayList<>();
		for (Object[] r : results) {
			int month = (Integer) r[0];
			long count = ((Number) r[1]).longValue();
			BigDecimal revenue = r[2] != null ? (BigDecimal) r[2] : BigDecimal.ZERO;
			chartData.add(new ChartData("Tháng " + month, count, revenue));
		}
		return chartData;
	}
	@Override
	public PerformanceStats getPerformanceStats(Long shipperId) {
		List<Object[]> results = orderRepository.getPerformanceStats(shipperId);
		if (results.isEmpty())
			return new PerformanceStats(0, 0, 0);
		Object[] r = results.get(0);
		return new PerformanceStats(((Number) r[0]).longValue(), ((Number) r[1]).longValue(),
				((Number) r[2]).longValue());
	}

	@Override
	public List<OrderItem> getOrderItems(Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));
		return orderItemRepository.findByOrder(order);
	}

	@Override
	public void assignShipper(Long orderId, Long shipperUserId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));
		if (order.getShipper() != null) {
			throw new RuntimeException("Đơn hàng này đã được phân công shipper rồi!");
		}
		if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
			throw new RuntimeException("Không thể phân công shipper cho đơn hàng đã giao hoặc đã hủy!");
		}

		if (order.getStatus() != OrderStatus.CONFIRMED) {
	        throw new RuntimeException("Chỉ có thể phân công shipper khi đơn đã được xác nhận!");
	    }

		Shipper shipper = shipperRepository.findByUser_UserId(shipperUserId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy shipper!"));
		if (shipper == null) {
		    throw new RuntimeException("Không tìm thấy shipper!");
		}
		order.setShipper(shipper);
		order.setStatus(OrderStatus.SHIPPING);
		orderRepository.save(order);
	}

	@Override
	public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));
		OrderStatus oldStatus = order.getStatus();
		if (order.getStatus() == OrderStatus.CANCELLED) {
			throw new RuntimeException("Không thể cập nhật trạng thái cho đơn hàng đã hủy!");
		}
		if (newStatus.ordinal() < oldStatus.ordinal()) {
	        throw new RuntimeException("Không thể cập nhật trạng thái lùi!");
	    }

	    // Chặn hủy nếu shipper đang giao
	    if (newStatus == OrderStatus.CANCELLED && oldStatus == OrderStatus.SHIPPING) {
	        throw new RuntimeException("Không thể hủy đơn hàng đang giao!");
	    }
		order.setStatus(newStatus);
		orderRepository.save(order);
	}

	@Override
	public void deleteOrder(Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));

		orderRepository.delete(order);
	}

	@Override
	@Transactional
	public Page<Order> filterOrders(OrderStatus status, String keyword, LocalDate date, LocalDate startDate,
			LocalDate endDate, int page) {
		Specification<Order> spec = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			// trạng thái
			if (status != null) {
				predicates.add(cb.equal(root.get("status"), status));
			}

			// tìm kiếm: nếu keyword parse được Long -> tìm orderId; else tìm user.fullName
			// LIKE
			if (keyword != null && !keyword.trim().isEmpty()) {
				String kw = keyword.trim();
				try {
					Long id = Long.parseLong(kw);
					predicates.add(cb.equal(root.get("orderId"), id));
				} catch (NumberFormatException ex) {
					predicates.add(cb.like(cb.lower(root.get("user").get("fullName")), "%" + kw.toLowerCase() + "%"));
				}
			}

			// ngày cụ thể
			if (date != null) {
				LocalDateTime s = date.atStartOfDay();
				LocalDateTime e = date.atTime(23, 59, 59);
				predicates.add(cb.between(root.get("orderDate"), s, e));
			}

			// khoảng
			if (startDate != null && endDate != null) {
				LocalDateTime s = startDate.atStartOfDay();
				LocalDateTime e = endDate.atTime(23, 59, 59);
				predicates.add(cb.between(root.get("orderDate"), s, e));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};

		Pageable pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by(Sort.Direction.DESC, "orderDate"));
		return orderRepository.findAll(spec, pageable);
	}

	@Override
	public Order findById(Long orderId) {
		return orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
	}

}
