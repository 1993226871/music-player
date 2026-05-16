package com.music.controller;

import com.music.common.Result;
import com.music.service.NeteaseApiService;
import com.music.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private NeteaseApiService neteaseApiService;

    @PostMapping("/upload-cookie")
    public Result<Map<String, Object>> uploadCookie(@RequestBody Map<String, String> body) {
        try {
            String content = body.get("cookie");
            if (content == null || content.length() < 100) {
                return Result.error("Cookie内容太短（需至少100字符），请确认复制完整");
            }

            // 先验证cookie是否有效
            Map<String, Object> validation = neteaseApiService.validateCookie(content.trim());
            if (!(boolean) validation.get("valid")) {
                return Result.error("Cookie无效: " + validation.get("message"));
            }

            // 保存cookie
            userService.updateAdminCookie(content.trim());
            neteaseApiService.resetCookieExpired();

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Cookie更新成功！" + validation.get("message"));
            result.put("length", content.length());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @PostMapping("/validate-cookie")
    public Result<Map<String, Object>> validateCookie(@RequestBody Map<String, String> body) {
        try {
            String content = body.get("cookie");
            Map<String, Object> validation = neteaseApiService.validateCookie(content.trim());
            return Result.success(validation);
        } catch (Exception e) {
            return Result.error("验证失败: " + e.getMessage());
        }
    }

    @GetMapping("/cookie-status")
    public Result<Map<String, Object>> getCookieStatus() {
        String cookie = userService.getAdminCookie();
        Map<String, Object> result = new HashMap<>();
        result.put("hasCookie", cookie != null && !cookie.isEmpty());
        result.put("cookieLength", cookie != null ? cookie.length() : 0);
        return Result.success(result);
    }
}