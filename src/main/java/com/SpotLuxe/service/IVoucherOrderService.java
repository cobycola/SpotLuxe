package com.SpotLuxe.service;

import com.SpotLuxe.dto.Result;
import com.SpotLuxe.entity.VoucherOrder;

public interface IVoucherOrderService {

    Result seckillVoucher(Long voucherId);

    void createVoucherOrder(VoucherOrder voucherOrder);
}
