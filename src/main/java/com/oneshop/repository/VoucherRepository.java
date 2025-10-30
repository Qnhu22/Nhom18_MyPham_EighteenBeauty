package com.oneshop.repository;

import com.oneshop.entity.Voucher;
import com.oneshop.enums.VoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    List<Voucher> findByStatusAndEndDateAfter(VoucherStatus status, LocalDateTime now);
    Optional<Voucher> findByCode(String code);
}
