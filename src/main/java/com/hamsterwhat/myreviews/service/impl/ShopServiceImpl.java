package com.hamsterwhat.myreviews.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hamsterwhat.myreviews.dto.Result;
import com.hamsterwhat.myreviews.entity.Shop;
import com.hamsterwhat.myreviews.mapper.ShopMapper;
import com.hamsterwhat.myreviews.service.IShopService;
import com.hamsterwhat.myreviews.utils.CacheClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static com.hamsterwhat.myreviews.utils.RedisConstants.*;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CacheClient cacheClient;

    @Override
    public Result queryById(Long id) {
        Shop shop;
        try {
            shop = queryByIdWithLogicExpiry(id);
        } catch (InterruptedException e) {
            shop = null;
        }
        return Result.ok(shop);
    }

    public Shop queryByIdWithPassThrough(Long id) {
        String shopKey = CACHE_SHOP_KEY + id;
        return cacheClient.getWithPassThrough(shopKey, Shop.class,
                () -> this.getById(id),
                CACHE_SHOP_TTL, TimeUnit.MINUTES);
    }

    public Shop queryByIdWithMutex(Long id) throws InterruptedException {
        String shopKey = CACHE_SHOP_KEY + id;
        String lockKey = LOCK_SHOP_KEY + id;
        return cacheClient.getWithMutex(shopKey, lockKey, Shop.class,
                () -> this.getById(id),
                CACHE_SHOP_TTL, TimeUnit.MINUTES, LOCK_SHOP_TTL);
    }

    public Shop queryByIdWithLogicExpiry(Long id) throws InterruptedException {
        String shopKey = CACHE_SHOP_KEY + id;
        String lockKey = LOCK_SHOP_KEY + id;
        return cacheClient.getWithLogicExpiry(shopKey, lockKey, Shop.class,
                () -> this.getById(id),
                CACHE_SHOP_TTL, TimeUnit.MINUTES, LOCK_SHOP_TTL);
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("Cannot find shop!");
        }

        String shopKey = CACHE_SHOP_KEY + id;

        updateById(shop);
        stringRedisTemplate.delete(shopKey);

        return Result.ok();
    }

}
