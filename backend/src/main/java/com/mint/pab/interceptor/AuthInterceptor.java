package com.mint.pab.interceptor;

import com.mint.pab.exception.BusinessException;
import com.mint.pab.exception.ErrorCode;
import com.mint.pab.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String USER_ID_ATTR = "userId";
    private static final String USERNAME_ATTR = "username";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith(TOKEN_PREFIX)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        String token = authorization.substring(TOKEN_PREFIX.length());

        try {
            if (jwtUtil.isTokenExpired(token)) {
                throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
            }

            Long userId = jwtUtil.getUserId(token);
            String username = jwtUtil.parseToken(token).get("username", String.class);

            String redisKey = "monit:token:" + userId;
            String storedToken = stringRedisTemplate.opsForValue().get(redisKey);

            if (storedToken == null || !storedToken.equals(token)) {
                throw new BusinessException(ErrorCode.TOKEN_INVALID);
            }

            request.setAttribute(USER_ID_ATTR, userId);
            request.setAttribute(USERNAME_ATTR, username);
            return true;

        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        } catch (BusinessException e) {
            throw e;
        } catch (JwtException e) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        } catch (Exception e) {
            log.error("Token验证异常: ", e);
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }

}
