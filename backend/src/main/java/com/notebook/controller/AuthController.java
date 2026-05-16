package com.notebook.controller;

import com.notebook.common.Result;
import com.notebook.dto.LoginRequest;
import com.notebook.dto.LoginResponse;
import com.notebook.dto.RegisterRequest;
import com.notebook.dto.UserVO;
import com.notebook.entity.User;
import com.notebook.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<String> register(@Validated @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request.getUsername(), request.getPassword());
        return Result.success("登录成功", response);
    }

    @GetMapping("/userinfo")
    public Result<UserVO> getUserInfo(@AuthenticationPrincipal User user) {
        UserVO userVO = userService.getUserInfo(user.getId());
        return Result.success(userVO);
    }

    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success("退出成功");
    }
}
