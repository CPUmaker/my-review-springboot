package com.hamsterwhat.myreviews.config;

import com.hamsterwhat.myreviews.utils.LoginInterceptor;
import com.hamsterwhat.myreviews.utils.TokenRefreshInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class SpringMvcConfig implements WebMvcConfigurer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api", HandlerTypePredicate.forAnnotation(RestController.class));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TokenRefreshInterceptor(stringRedisTemplate))
                .addPathPatterns("/api/**");
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/blog/hot",
                        "/api/shop/**",
                        "/api/shop-type/**",
                        "/api/user/login",
                        "/api/user/code",
                        "/api/voucher/**"
                );
    }
}