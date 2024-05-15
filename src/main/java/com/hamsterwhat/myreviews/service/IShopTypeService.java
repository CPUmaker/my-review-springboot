package com.hamsterwhat.myreviews.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hamsterwhat.myreviews.dto.Result;
import com.hamsterwhat.myreviews.entity.ShopType;

public interface IShopTypeService extends IService<ShopType> {

    Result queryTypeList();
}
