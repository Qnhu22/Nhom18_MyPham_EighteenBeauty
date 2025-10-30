package com.oneshop.service.impl;

import com.oneshop.entity.Voucher;
import com.oneshop.enums.DiscountType;
import com.oneshop.enums.VoucherStatus;

import com.oneshop.repository.VoucherRepository;
import com.oneshop.service.VoucherService;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;      // ✅ Thêm import này
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VoucherServiceImpl implements VoucherService {

    @Autowired
    private VoucherRepository voucherRepo;
	@Autowired
	private VoucherRepository repo;

	// ✅ Tính trạng thái dựa theo điều kiện
	private VoucherStatus calcStatus(Voucher v) {
		LocalDateTime now = LocalDateTime.now();

		if (v.getStatus() == VoucherStatus.DISABLED)
			return VoucherStatus.DISABLED;

		if (v.getUsageLimit() != null && v.getUsedCount() != null && v.getUsedCount() >= v.getUsageLimit())
			return VoucherStatus.USED_UP;

		if (v.getStartDate() != null && now.isBefore(v.getStartDate()))
			return VoucherStatus.INACTIVE;

		if (v.getEndDate() != null && now.isAfter(v.getEndDate()))
			return VoucherStatus.EXPIRED;

		return VoucherStatus.ACTIVE;
	}

	// ✅ Luôn đảm bảo DB lưu trạng thái đúng
	private void ensurePersistActualStatus(Voucher v) {
		VoucherStatus actual = calcStatus(v);
		if (v.getStatus() != actual) {
			v.setStatus(actual);
			repo.save(v); // ✅ update DB
		}
	}

    @Override
    public List<Voucher> getAllVouchers() {
        return voucherRepo.findAll();
    }
	@Override
	@Transactional
	public Page<Voucher> filterVouchers(String keyword, DiscountType discountType, VoucherStatus status, int page) {

		Pageable pageable = PageRequest.of(Math.max(page, 0), 10, Sort.by(Sort.Direction.DESC, "createdAt"));

		Specification<Voucher> spec = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			// 🔍 Tìm kiếm theo ID hoặc tên hoặc mã
			if (keyword != null && !keyword.trim().isEmpty()) {
				String kw = keyword.trim();
				try {
					Long id = Long.parseLong(kw);
					predicates.add(cb.equal(root.get("code"), id));
				} catch (NumberFormatException ex) {
					String k = "%" + kw.toLowerCase() + "%";
					predicates
							.add(cb.or(cb.like(cb.lower(root.get("name")), k), cb.like(cb.lower(root.get("code")), k)));
				}
			}

			// 🎯 Lọc theo loại discount
			if (discountType != null) {
			    predicates.add(cb.equal(root.get("discountType"), discountType));
			}

			// 🟦 Lọc theo trạng thái
			if (status != null) {
				predicates.add(cb.equal(root.get("status"), status));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};

		Page<Voucher> pageResult = repo.findAll(spec, pageable);

		// ✅ Cập nhật trạng thái thật (nếu status thay đổi theo thời gian)
		pageResult.forEach(this::ensurePersistActualStatus);

		return pageResult;
	}

    @Override
    public List<Voucher> getActiveVouchers() {
        return voucherRepo.findByStatusAndEndDateAfter(VoucherStatus.ACTIVE, LocalDateTime.now());
    }
	@Override
	public Optional<Voucher> getById(Long id) {
		return repo.findById(id).map(v -> {
			ensurePersistActualStatus(v);
			return v;
		});
	}

    @Override
    public Voucher getVoucherByCode(String code) {
        return voucherRepo.findByCode(code).orElse(null);
    }
	@Override
	@Transactional
	public Voucher saveOrUpdate(Voucher v) {
		Voucher entity;
	    if (v.getVoucherId() != null) {
	        entity = repo.findById(v.getVoucherId()).orElseThrow(() -> new RuntimeException("Voucher not found"));
	        // Chỉ cập nhật các field nhận từ form
	        entity.setName(v.getName());
	        entity.setDiscountType(v.getDiscountType());
	        entity.setDiscountAmount(v.getDiscountAmount());
	        entity.setDiscountPercent(v.getDiscountPercent());
	        entity.setMaxDiscountValue(v.getMaxDiscountValue());
	        entity.setMinOrderValue(v.getMinOrderValue());
	        entity.setStartDate(v.getStartDate());
	        entity.setEndDate(v.getEndDate());
	        entity.setUsageLimit(v.getUsageLimit());
	        entity.setDescription(v.getDescription());
	        entity.setCode(v.getCode());
	        entity.setStatus(v.getStatus());
	        entity.setUsedCount(v.getUsedCount());
	    } else {
	        entity = v;
	        entity.setCreatedAt(LocalDateTime.now());
	        if (entity.getUsedCount() == null)
	            entity.setUsedCount(0);
	        if (entity.getStatus() == null)
	            entity.setStatus(VoucherStatus.INACTIVE);
	    }

	    Voucher saved = repo.save(entity);
	    ensurePersistActualStatus(saved);
	    return saved;
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
    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
	}
}
