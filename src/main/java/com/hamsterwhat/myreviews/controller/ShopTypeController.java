package com.hamsterwhat.myreviews.controller;


import com.hamsterwhat.myreviews.dto.Result;
import com.hamsterwhat.myreviews.entity.ShopType;
import com.hamsterwhat.myreviews.service.IShopTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Autowired
    private IShopTypeService typeService;

    @GetMapping("list")
    public Result queryTypeList() {
        return typeService.queryTypeList();
    }
}
