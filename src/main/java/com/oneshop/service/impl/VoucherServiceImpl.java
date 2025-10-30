package com.oneshop.service.impl;

import com.oneshop.entity.Voucher;
import com.oneshop.enums.DiscountType;
import com.oneshop.enums.VoucherStatus;
import com.oneshop.repository.VoucherRepository;
import com.oneshop.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;      // ✅ Thêm import này
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoucherServiceImpl implements VoucherService {

    @Autowired
    private VoucherRepository voucherRepo;

    @Override
    public List<Voucher> getAllVouchers() {
        return voucherRepo.findAll();
    }

    @Override
    public List<Voucher> getActiveVouchers() {
        return voucherRepo.findByStatusAndEndDateAfter(VoucherStatus.ACTIVE, LocalDateTime.now());
    }

    @Override
    public Voucher getVoucherByCode(String code) {
        return voucherRepo.findByCode(code).orElse(null);
    }

    // ✅ Tính giá trị giảm
    public BigDecimal calculateDiscount(Voucher v, BigDecimal total) {
        if (v == null) return BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;

        if (v.getDiscountType() == DiscountType.PERCENT) {
            discount = total.multiply(BigDecimal.valueOf(v.getDiscountPercent()))
                            .divide(BigDecimal.valueOf(100));
            if (v.getMaxDiscountValue() != null &&
                discount.compareTo(v.getMaxDiscountValue()) > 0)
                discount = v.getMaxDiscountValue();
        } else if (v.getDiscountType() == DiscountType.AMOUNT) {
            discount = v.getDiscountAmount();
        }
        return discount.max(BigDecimal.ZERO);
    }
}
