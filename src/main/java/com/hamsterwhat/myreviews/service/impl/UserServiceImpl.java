package com.hamsterwhat.myreviews.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hamsterwhat.myreviews.dto.LoginFormDTO;
import com.hamsterwhat.myreviews.dto.Result;
import com.hamsterwhat.myreviews.dto.UserDTO;
import com.hamsterwhat.myreviews.entity.User;
import com.hamsterwhat.myreviews.mapper.UserMapper;
import com.hamsterwhat.myreviews.service.IUserService;
import com.hamsterwhat.myreviews.utils.RegexUtils;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hamsterwhat.myreviews.utils.RedisConstants.*;
import static com.hamsterwhat.myreviews.utils.SystemConstants.USER_NICK_NAME_PREFIX;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("Invalid phone number!");
        }

        String code = RandomUtil.randomNumbers(6);
        String key = LOGIN_CODE_KEY + phone;
        redisTemplate.opsForValue().set(key, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);

        log.info("Send code to {}: {}", phone, code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("Invalid phone number!");
        }

        String cacheCode = redisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();

        if (cacheCode != null && !cacheCode.equals(code)) {
            return Result.fail("Invalid login code!");
        }

        User user = lambdaQuery().eq(User::getPhone, phone).one();
        if (user == null) {
            user = createUserWithPhone(phone);
        }

        String token = UUID.fastUUID().toString(true);
        String userKey = LOGIN_USER_KEY + token;
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(
                userDTO,
                new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fn, fv) -> fv.toString())
        );

        redisTemplate.opsForHash().putAll(userKey, userMap);
        redisTemplate.expire(userKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

        return Result.ok(token);
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));

        save(user);

        return user;
    }
}
