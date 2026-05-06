package com.mint.pab.service;

import com.mint.pab.dto.LoginRequest;
import com.mint.pab.dto.LoginResponse;
import com.mint.pab.entity.User;
import com.mint.pab.exception.BusinessException;
import com.mint.pab.exception.ErrorCode;
import com.mint.pab.repository.UserMapper;
import com.mint.pab.util.JwtUtil;
import com.mint.pab.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordUtil passwordUtil;

    @Value("${jwt.expiration}")
    private Long expiration;

    private static final String LOGIN_LOCK_PREFIX = "monit:login:lock:";
    private static final String LOGIN_FAIL_PREFIX = "monit:login:fail:";
    private static final String TOKEN_PREFIX = "monit:token:";

    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String lockKey = LOGIN_LOCK_PREFIX + username;

        // 1. 检查账户是否被锁定
        Boolean locked = stringRedisTemplate.hasKey(lockKey);
        if (Boolean.TRUE.equals(locked)) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }

        // 2. 根据 username 查询用户
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );

        // 3. 用户不存在或密码不匹配
        if (user == null || !passwordUtil.matches(request.getPassword(), user.getPassword())) {
            String failKey = LOGIN_FAIL_PREFIX + username;
            Long failCount = stringRedisTemplate.opsForValue().increment(failKey);
            if (failCount != null && failCount == 1) {
                stringRedisTemplate.expire(failKey, 30, TimeUnit.MINUTES);
            }

            // 4. 失败次数>=5 → 设置锁定标记
            if (failCount != null && failCount >= 5) {
                stringRedisTemplate.opsForValue().set(lockKey, "1", 30, TimeUnit.MINUTES);
                throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
            }

            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 5. 密码匹配 → 清除失败计数，生成JWT Token
        String failKey = LOGIN_FAIL_PREFIX + username;
        stringRedisTemplate.delete(failKey);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(expiration);

        // 6. 存储Token到Redis
        String tokenKey = TOKEN_PREFIX + user.getId();
        stringRedisTemplate.opsForValue().set(tokenKey, token, 7, TimeUnit.DAYS);

        // 7. 更新用户 lastLoginTime, loginFailCount=0
        user.setLastLoginTime(LocalDateTime.now());
        user.setLoginFailCount(0);
        userMapper.updateById(user);

        // 8. 返回 LoginResponse
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setExpireTime(expireTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return response;
    }

    public void logout(Long userId) {
        String tokenKey = TOKEN_PREFIX + userId;
        stringRedisTemplate.delete(tokenKey);
    }

    public LoginResponse refreshToken(Long userId, String username) {
        String token = jwtUtil.generateToken(userId, username);
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(expiration);

        String tokenKey = TOKEN_PREFIX + userId;
        stringRedisTemplate.opsForValue().set(tokenKey, token, 7, TimeUnit.DAYS);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setExpireTime(expireTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return response;
    }
}
