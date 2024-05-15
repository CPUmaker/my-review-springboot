package com.hamsterwhat.myreviews.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hamsterwhat.myreviews.entity.Follow;
import com.hamsterwhat.myreviews.mapper.FollowMapper;
import com.hamsterwhat.myreviews.service.IFollowService;
import org.springframework.stereotype.Service;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

}
