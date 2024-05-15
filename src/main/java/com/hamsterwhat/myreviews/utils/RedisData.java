package com.hamsterwhat.myreviews.utils;

import lombok.Data;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Data
public class RedisData <T> {
    private LocalDateTime expireTime;
    private T data;
}
