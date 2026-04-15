package com.student.scm.interceptor;

import com.student.scm.context.BaseContext;
import com.student.scm.properties.JwtProperties;
import com.student.scm.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {
    private JwtProperties jwtProperties;

    public JwtTokenInterceptor(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            // 拦截到非Controller动态方法
            return true;
        }

        // 2. 从请求头 (Header) 中获取前端传来的 token
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        // 3. 判断 token 是否为空
        if (!StringUtils.hasText(token)) {
            log.error("Token 解析失败或已过期: {}", request.getRequestURI());
            response.setStatus(401);
            return false; // 拦截
        }

        // 4. 解析并验证 Token
        try {
            log.info("jwt校验:{}", token);
            // 调用你的解析方法，如果过期或被篡改，这里会直接抛出异常
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long userId = claims.get("userId", Long.class);
            request.setAttribute("currentUserId", claims.get("userId", Long.class));
            BaseContext.setCurrentId(userId);
            request.setAttribute("currentRoleId", claims.get("roleId", Long.class));

            // 可以把解析出来的 userId 打印出来看
            log.info("Token 校验通过，当前操作用户ID: {}", userId);

            // 校验通过，放行！
            return true;
        } catch (Exception e) {
            log.error("Token 解析失败或已过期: {}", e.getMessage());
            response.setStatus(401);
            return false; // 拦截
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理 ThreadLocal，避免数据残留
        BaseContext.removeCurrentId();
    }
}