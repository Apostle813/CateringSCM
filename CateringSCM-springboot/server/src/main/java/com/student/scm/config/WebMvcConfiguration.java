package com.student.scm.config;

import com.student.scm.interceptor.JwtTokenInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 相关配置类，用于注册拦截器
 */
@Configuration
@Slf4j
public class WebMvcConfiguration implements WebMvcConfigurer {
    private JwtTokenInterceptor jwtTokenInterceptor;

    public WebMvcConfiguration(JwtTokenInterceptor jwtTokenInterceptor) {
        this.jwtTokenInterceptor = jwtTokenInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/**")             // 拦截所有请求
                .excludePathPatterns("/user/login") // 排除登录接口
                .excludePathPatterns("/user/exists") // 忘记密码 — 检查账号
                .excludePathPatterns("/user/reset-password") // 忘记密码 — 重置密码
                .excludePathPatterns("/swagger-ui/**") // 排除Swagger
                .excludePathPatterns("/v3/**") // 排除OpenAPI
                .excludePathPatterns("/file/upload"); // 排除文件上传
    }
}