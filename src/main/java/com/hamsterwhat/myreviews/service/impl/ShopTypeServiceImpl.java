package com.hamsterwhat.myreviews.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hamsterwhat.myreviews.dto.Result;
import com.hamsterwhat.myreviews.entity.ShopType;
import com.hamsterwhat.myreviews.mapper.ShopTypeMapper;
import com.hamsterwhat.myreviews.service.IShopTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hamsterwhat.myreviews.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;
import static com.hamsterwhat.myreviews.utils.RedisConstants.CACHE_SHOP_TYPE_TTL;

@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {
        List<String> typeJsonList = stringRedisTemplate.opsForList()
                .range(CACHE_SHOP_TYPE_KEY, 0, -1);

        if (typeJsonList != null && !typeJsonList.isEmpty()) {
            List<ShopType> typeList = typeJsonList
                    .stream()
                    .map(shopType -> JSONUtil.toBean(shopType, ShopType.class))
                    .toList();
            stringRedisTemplate.expire(CACHE_SHOP_TYPE_KEY, CACHE_SHOP_TYPE_TTL, TimeUnit.MINUTES);
            return Result.ok(typeList);
        }

        List<ShopType> typeList = query().orderByAsc("sort").list();
        typeJsonList = typeList
                .stream()
                .map(JSONUtil::toJsonStr)
                .toList();

        stringRedisTemplate.opsForList().rightPushAll(CACHE_SHOP_TYPE_KEY, typeJsonList);
        stringRedisTemplate.expire(CACHE_SHOP_TYPE_KEY, CACHE_SHOP_TYPE_TTL, TimeUnit.MINUTES);

        return Result.ok(typeList);
    }
}
