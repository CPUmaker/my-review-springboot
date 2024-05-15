package com.hamsterwhat.myreviews.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hamsterwhat.myreviews.dto.LoginFormDTO;
import com.hamsterwhat.myreviews.dto.Result;
import com.hamsterwhat.myreviews.entity.User;
import jakarta.servlet.http.HttpSession;

public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);
}
