package com.music.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.music.common.JwtUtil;
import com.music.entity.User;
import com.music.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public Map<String, Object> register(String username, String password) {
        if (userMapper.selectCount(new QueryWrapper<User>().eq("username", username)) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        userMapper.insert(user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", JwtUtil.generateToken(user.getId(), username));
        result.put("userId", user.getId());
        result.put("username", username);
        return result;
    }

    public Map<String, Object> login(String username, String password) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null || !encoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("token", JwtUtil.generateToken(user.getId(), username));
        result.put("userId", user.getId());
        result.put("username", username);
        return result;
    }

    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    public String getAdminCookie() {
        User admin = userMapper.selectOne(new QueryWrapper<User>().eq("role", "admin"));
        if (admin != null && admin.getNeteaseCookie() != null && !admin.getNeteaseCookie().isEmpty()) {
            return admin.getNeteaseCookie();
        }
        return "";
    }

    public void updateAdminCookie(String cookie) {
        User admin = userMapper.selectOne(new QueryWrapper<User>().eq("role", "admin"));
        if (admin != null) {
            admin.setNeteaseCookie(cookie);
            userMapper.updateById(admin);
        }
    }
}
