package com.student.scm.interceptor;

import com.student.scm.properties.JwtProperties;
import com.student.scm.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT 令牌拦截器
 */
@Setter
@Component
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {
    private JwtProperties jwtProperties;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 放行前端的跨域预检请求 (OPTIONS)
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 2. 从请求头 (Header) 中获取前端传来的 token
        // 假设前端统一把 token 放在名为 "token" 的 Header 中
        String token = request.getHeader("token");

        // 3. 判断 token 是否为空
        if (!StringUtils.hasText(token)) {
            log.error("拦截到未携带Token的请求: {}", request.getRequestURI());
            response.setStatus(401); // 401 Unauthorized 表示未认证
            return false; // 拦截，不放行
        }

        // 4. 解析并验证 Token
        try {
            log.info("开始校验Token...");
            // 调用你的解析方法，如果过期或被篡改，这里会直接抛出异常
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            request.setAttribute("currentUserId", claims.get("userId", Long.class));
            request.setAttribute("currentRoleId", claims.get("roleId", Long.class));

            // 可以把解析出来的 userId 打印出来看看
            Long userId = claims.get("userId", Long.class);
            log.info("Token 校验通过，当前操作用户ID: {}", userId);

            // 校验通过，放行！
            return true;
        } catch (Exception e) {
            log.error("Token 解析失败或已过期: {}", e.getMessage());
            response.setStatus(401);
            return false; // 拦截
        }
    }
}