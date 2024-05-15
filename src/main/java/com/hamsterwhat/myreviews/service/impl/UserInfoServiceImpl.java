package com.hamsterwhat.myreviews.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hamsterwhat.myreviews.entity.UserInfo;
import com.hamsterwhat.myreviews.mapper.UserInfoMapper;
import com.hamsterwhat.myreviews.service.IUserInfoService;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
