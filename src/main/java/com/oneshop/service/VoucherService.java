package com.oneshop.service;


import com.oneshop.entity.Voucher;
import com.oneshop.enums.DiscountType;
import com.oneshop.enums.VoucherStatus;
import org.springframework.data.domain.Page;

//import java.util.List;
import java.util.Optional;
public interface VoucherService {
    
    Page<Voucher> filterVouchers(String keyword, DiscountType discountType, VoucherStatus status, int page);
    Optional<Voucher> getById(Long id);
    Voucher saveOrUpdate(Voucher v);
    void deleteById(Long id);
}
