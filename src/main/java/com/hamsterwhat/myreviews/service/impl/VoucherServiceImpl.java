package com.hamsterwhat.myreviews.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hamsterwhat.myreviews.dto.Result;
import com.hamsterwhat.myreviews.entity.SeckillVoucher;
import com.hamsterwhat.myreviews.entity.Voucher;
import com.hamsterwhat.myreviews.mapper.VoucherMapper;
import com.hamsterwhat.myreviews.service.ISeckillVoucherService;
import com.hamsterwhat.myreviews.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.hamsterwhat.myreviews.utils.RedisConstants.SECKILL_STOCK_KEY;

@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Autowired
    private ISeckillVoucherService seckillVoucherService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        // 返回结果
        return Result.ok(vouchers);
    }

    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {

        save(voucher);

        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);

        String key = SECKILL_STOCK_KEY + voucher.getId();
        stringRedisTemplate.opsForValue().set(key, voucher.getStock().toString());
    }
}
