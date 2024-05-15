package com.hamsterwhat.myreviews.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hamsterwhat.myreviews.dto.Result;
import com.hamsterwhat.myreviews.entity.SeckillVoucher;
import com.hamsterwhat.myreviews.entity.VoucherOrder;
import com.hamsterwhat.myreviews.mapper.VoucherOrderMapper;
import com.hamsterwhat.myreviews.service.ISeckillVoucherService;
import com.hamsterwhat.myreviews.service.IVoucherOrderService;
import com.hamsterwhat.myreviews.utils.RedisIdWorker;
import com.hamsterwhat.myreviews.utils.UserHolder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.*;

import static com.hamsterwhat.myreviews.utils.RedisConstants.SECKILL_ORDER_KEY;
import static com.hamsterwhat.myreviews.utils.RedisConstants.SECKILL_STOCK_KEY;

@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private final BlockingQueue<VoucherOrder> orderQueue = new LinkedBlockingQueue<>(1024 * 1024);

    @Autowired
    private ISeckillVoucherService seckillVoucherService;

    @Autowired
    private RedisIdWorker redisIdWorker;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    private IVoucherOrderService proxy;

    @PostConstruct
    private void init() {
        EXECUTOR_SERVICE.submit(() -> {
            while (true) {
                try {
                    VoucherOrder order = orderQueue.take();
                    proxy.createVoucherOrderForAsyncMQ(order);
                } catch (InterruptedException e) {
                    log.error("Something happened during processing order:", e);
                    break;
                }
            }
        });
    }

    public Result seckillVoucherWithAsyncMQ(Long voucherId) {
        Long userId = UserHolder.getUser().getId();

        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString(), SECKILL_STOCK_KEY, SECKILL_ORDER_KEY
        );

        assert result != null;
        if (result == 1) {
            return Result.fail("The stock is not enough!");
        }
        if (result == 2) {
            return Result.fail("You have already ordered it!");
        }

        proxy = (IVoucherOrderService) AopContext.currentProxy();

        long orderId = redisIdWorker.nextId("voucherOrder");

        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(redisIdWorker.nextId("voucherOrder"));
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        voucherOrder.setStatus(1);
        orderQueue.add(voucherOrder);

        return Result.ok(orderId);
    }

    @Override
    public Result seckillVoucher(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);

        LocalDateTime currentTime = LocalDateTime.now();
        if (currentTime.isBefore(seckillVoucher.getBeginTime()) || currentTime.isAfter(seckillVoucher.getEndTime())) {
            return Result.fail("The time is not permitted to use this voucher!");
        }

        if (seckillVoucher.getStock() <= 0) {
            return Result.fail("The stock is not enough!");
        }

        String lockName = String.join(":",
                "lock", "order", userId.toString(), seckillVoucher.getVoucherId().toString());
        RLock lock = redissonClient.getLock(lockName);
        if (!lock.tryLock()) {
            return Result.fail("Something wrong with the network!");
        }

        try {
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public Result createVoucherOrder(Long voucherId) {
        Long userId = UserHolder.getUser().getId();

        boolean isVoucherOrdered = lambdaQuery().eq(VoucherOrder::getUserId, userId)
                .eq(VoucherOrder::getVoucherId, voucherId)
                .exists();
        if (isVoucherOrdered) {
            return Result.fail("The voucher already exists!");
        }

        seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)
                .update();

        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(redisIdWorker.nextId("voucherOrder"));
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        voucherOrder.setStatus(1);

        this.save(voucherOrder);

        return Result.ok(voucherOrder.getId());
    }

    @Transactional
    public Result createVoucherOrderForAsyncMQ(VoucherOrder voucherOrder) {
        seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherOrder.getVoucherId())
                .gt("stock", 0)
                .update();

        this.save(voucherOrder);

        return Result.ok(voucherOrder.getId());
    }
}
