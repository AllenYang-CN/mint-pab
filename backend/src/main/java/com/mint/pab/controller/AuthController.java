package com.mint.pab.controller;

import com.mint.pab.dto.LoginRequest;
import com.mint.pab.dto.LoginResponse;
import com.mint.pab.dto.Result;
import com.mint.pab.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        authService.logout(userId);
        return Result.success();
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        LoginResponse response = authService.refreshToken(userId, username);
        return Result.success(response);
    }
}
