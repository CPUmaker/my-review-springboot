package com.hamsterwhat.myreviews.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hamsterwhat.myreviews.dto.Result;
import com.hamsterwhat.myreviews.entity.Shop;

public interface IShopService extends IService<Shop> {

    Result queryById(Long id);

    Result update(Shop shop);
}
