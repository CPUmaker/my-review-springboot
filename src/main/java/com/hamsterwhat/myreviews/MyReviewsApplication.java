package com.hamsterwhat.myreviews;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan({"com.hamsterwhat.myreviews.mapper"})
@EnableAspectJAutoProxy(exposeProxy = true)
public class MyReviewsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyReviewsApplication.class, args);
    }

}
