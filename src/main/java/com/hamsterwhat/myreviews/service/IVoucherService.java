package com.hamsterwhat.myreviews.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hamsterwhat.myreviews.dto.Result;
import com.hamsterwhat.myreviews.entity.Voucher;

public interface IVoucherService extends IService<Voucher> {

    Result queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(Voucher voucher);
}
