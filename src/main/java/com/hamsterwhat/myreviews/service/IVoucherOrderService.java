package com.hamsterwhat.myreviews.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hamsterwhat.myreviews.dto.Result;
import com.hamsterwhat.myreviews.entity.VoucherOrder;

public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucher(Long voucherId);

    Result createVoucherOrder(Long voucherId);

    Result createVoucherOrderForAsyncMQ(VoucherOrder voucherOrder);
}
