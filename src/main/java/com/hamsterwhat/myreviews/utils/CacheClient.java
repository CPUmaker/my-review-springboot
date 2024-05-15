package com.hamsterwhat.myreviews.utils;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.hamsterwhat.myreviews.utils.RedisConstants.*;

@Slf4j
@Component
public class CacheClient {
    private static final ExecutorService CACHE_RECOVER_EXECUTOR = Executors.newFixedThreadPool(10);

    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public <T> void set(String key, T value, Long time, TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, timeUnit);
    }

    public <T> void setWithLogicExpiry(String key, T value, Long time, TimeUnit timeUnit) {
        RedisData<T> redisData = new RedisData<>();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(timeUnit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    public <T> T getWithPassThrough(String key, Class<T> type, Supplier<T> dbCallback, Long time, TimeUnit timeUnit) {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);
        }
        if (json != null) {
            return null;
        }

        T result = dbCallback.get();
        if (result == null) {
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }

        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(result),
                time, timeUnit);

        return result;
    }

    public <T> T getWithMutex(String key, String lockKey, Class<T> type, Supplier<T> dbCallback,
                              Long time, TimeUnit timeUnit, Long lockTTLinSeconds) throws InterruptedException {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);
        }
        if (json != null) {
            return null;
        }

        boolean isLock = false;
        while (!isLock) {
            if (!(isLock = tryLock(lockKey, lockTTLinSeconds))) {
                Thread.sleep(50);
            }

            json = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(json)) {
                return JSONUtil.toBean(json, type);
            }
            if (json != null) {
                return null;
            }
        }

        T result;
        try {
            result = dbCallback.get();

            if (result == null) {
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }

            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(result), time, timeUnit);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            unlock(lockKey);
        }

        return result;
    }

    public <T> T getWithLogicExpiry(String key, String lockKey, Class<T> type, Supplier<T> dbCallback,
                              Long time, TimeUnit timeUnit, Long lockTTLinSeconds) {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(json)) {
            return null;
        }

        RedisData<JSONObject> redisData = JSONUtil.toBean(json, new TypeReference<>() {
        }, false);
        LocalDateTime expireTime = redisData.getExpireTime();
        T result = JSONUtil.toBean(redisData.getData(), type);

        if (expireTime.isAfter(LocalDateTime.now())) {
            return result;
        }

        boolean isLock = tryLock(lockKey, lockTTLinSeconds);
        if (!isLock) {
            return result;
        }

        // Double check
        json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(json)) {
            return null;
        }
        redisData = JSONUtil.toBean(json, new TypeReference<>() {
        }, false);
        if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
            return JSONUtil.toBean(redisData.getData(), type);
        }

        CacheClient.CACHE_RECOVER_EXECUTOR.submit(() -> {
            try {
                T data = dbCallback.get();
                if (data == null) {
                    stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                }
                setWithLogicExpiry(key, data, time, timeUnit);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                unlock(lockKey);
            }
        });

        return null;
    }



    private boolean tryLock(String key, Long timeInSeconds) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", timeInSeconds, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}
