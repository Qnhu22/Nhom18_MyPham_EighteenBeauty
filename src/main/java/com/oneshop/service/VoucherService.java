package com.oneshop.service;

import com.oneshop.entity.Voucher;
import java.util.List;

public interface VoucherService {
    List<Voucher> getAllVouchers();               // Lấy toàn bộ voucher
    List<Voucher> getActiveVouchers();            // Lấy voucher đang hoạt động
    Voucher getVoucherByCode(String code);        // Tìm voucher theo mã
}
