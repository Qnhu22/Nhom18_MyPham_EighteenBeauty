package com.oneshop.repository;


import com.oneshop.entity.Voucher;
import com.oneshop.enums.VoucherStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long>, JpaSpecificationExecutor<Voucher> {
	// 1️ Lấy danh sách voucher theo loại
    Page<Voucher> findByDiscountType(String type, Pageable pageable);

    // 2️ Lấy danh sách voucher theo trạng thái
    Page<Voucher> findByStatus(VoucherStatus status, Pageable pageable);

    // 3️ Kiểm tra trùng mã voucher
    boolean existsByCode(String code);
}
