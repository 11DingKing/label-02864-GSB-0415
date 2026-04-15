package com.exam.controller;

import com.exam.common.Result;
import com.exam.dto.LoginRequest;
import com.exam.dto.LoginResponse;
import com.exam.entity.User;
import com.exam.service.AuthService;
import com.exam.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @GetMapping("/info")
    public Result<User> getUserInfo() {
        User user = authService.getUserById(UserContext.getUserId());
        user.setPassword(null);
        return Result.success(user);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
