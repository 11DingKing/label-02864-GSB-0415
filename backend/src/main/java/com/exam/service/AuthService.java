package com.exam.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.common.BusinessException;
import com.exam.dto.LoginRequest;
import com.exam.dto.LoginResponse;
import com.exam.entity.User;
import com.exam.mapper.UserMapper;
import com.exam.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        String encryptedPassword = DigestUtil.md5Hex(request.getPassword());
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        log.info("用户登录成功: {}", user.getUsername());

        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .token(token)
                .build();
    }

    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
}
