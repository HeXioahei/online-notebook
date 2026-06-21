package com.example.notebook.controller;

import com.example.notebook.dto.ApiResponse;
import com.example.notebook.dto.UserLoginDTO;
import com.example.notebook.dto.UserRegisterDTO;
import com.example.notebook.entity.User;
import com.example.notebook.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public ApiResponse<User> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        try {
            User user = userService.register(registerDTO);
            user.setPassword(null);
            return ApiResponse.success("注册成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public ApiResponse<User> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        try {
            User user = userService.login(loginDTO);
            user.setPassword(null);
            return ApiResponse.success("登录成功", user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            user.setPassword(null);
            return ApiResponse.success(user);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}