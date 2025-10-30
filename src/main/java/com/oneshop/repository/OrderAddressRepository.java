package com.oneshop.repository;

import com.oneshop.entity.OrderAddress;
import com.oneshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderAddressRepository extends JpaRepository<OrderAddress, Long> {
	List<OrderAddress> findByUser_UserId(Long userId);

	List<OrderAddress> findByUser_EmailContainingIgnoreCaseOrReceiverNameContainingIgnoreCaseOrPhoneContainingIgnoreCase(
	        String email, String receiverName, String phone
	    );

    // Optional: chỉ lấy địa chỉ mặc định nếu cần dùng sau này
	List<OrderAddress> findByDefaultAddressTrueAndUser_UserId(Long userId);

	List<OrderAddress> findByUser(User user);
	Optional<OrderAddress> findFirstByUserAndDefaultAddressTrue(User user);

    Optional<OrderAddress> findByAddressIdAndUser(Long addressId, User user);
}
