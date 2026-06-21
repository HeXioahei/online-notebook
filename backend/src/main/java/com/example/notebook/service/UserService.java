package com.example.notebook.service;

import com.example.notebook.dto.UserLoginDTO;
import com.example.notebook.dto.UserRegisterDTO;
import com.example.notebook.entity.User;
import com.example.notebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public User register(UserRegisterDTO registerDTO) {
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        if (registerDTO.getEmail() != null && !registerDTO.getEmail().isEmpty()) {
            if (userRepository.existsByEmail(registerDTO.getEmail())) {
                throw new RuntimeException("邮箱已被使用");
            }
        }
        
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(registerDTO.getPassword());
        user.setEmail(registerDTO.getEmail());
        
        return userRepository.save(user);
    }
    
    public User login(UserLoginDTO loginDTO) {
        User user = userRepository.findByUsername(loginDTO.getUsername())
            .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        
        if (!user.getPassword().equals(loginDTO.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        return user;
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
}