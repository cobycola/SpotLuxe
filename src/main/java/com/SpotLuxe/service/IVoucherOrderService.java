package com.SpotLuxe.service;

import com.SpotLuxe.dto.Result;

public interface IVoucherOrderService {

    Result seckillVoucher(Long voucherId);

    Result createVoucherOrder(Long voucherId);
}
