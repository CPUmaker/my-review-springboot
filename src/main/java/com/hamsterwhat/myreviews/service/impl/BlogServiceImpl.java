package com.hamsterwhat.myreviews.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hamsterwhat.myreviews.entity.Blog;
import com.hamsterwhat.myreviews.mapper.BlogMapper;
import com.hamsterwhat.myreviews.service.IBlogService;
import org.springframework.stereotype.Service;

@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

}
