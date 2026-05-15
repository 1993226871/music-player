# 音乐播放器 - 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个类网易云音乐的桌面音乐播放器，用户可通过独立账号体系共享VIP账号的音乐资源

**Architecture:** 客户端(Tauri+Vue3)通过HTTP API与Spring Boot后端通信，后端调用NeteaseCloudMusicApi(Node.js)获取歌曲URL/歌词/搜索结果，用户体系完全独立于网易云

**Tech Stack:** Tauri 2.x + Vue 3 + Spring Boot 3.x + MyBatis-Plus + JWT + MySQL 8.x + NeteaseCloudMusicApi(Node.js)

---

## 项目目录结构

```
music-player/
├── client/                        # Tauri + Vue3 前端
│   ├── src/
│   │   ├── views/
│   │   ├── components/
│   │   ├── stores/
│   │   ├── router/
│   │   └── assets/
│   ├── public/
│   ├── package.json
│   └── tauri.conf.json
├── server/                        # Spring Boot 后端
│   ├── src/main/java/com/music/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── mapper/
│   │   ├── entity/
│   │   └── common/
│   ├── src/main/resources/
│   │   ├── mapper/
│   │   ├── application.yml
│   │   └── schema.sql
│   └── pom.xml
└── netease-api/                   # NeteaseCloudMusicApi (Node.js)
    ├── app.js
    ├── package.json
    └── routes/
```

---

## 阶段一：NeteaseCloudMusicApi 部署

### Task 1: 部署 NeteaseCloudMusicApi

**Files:**
- Create: `netease-api/package.json`
- Create: `netease-api/app.js`
- Create: `netease-api/.env`

- [ ] **Step 1: 创建项目目录和初始化**

```bash
cd "E:/music player"
mkdir netease-api
cd netease-api
npm init -y
```

- [ ] **Step 2: 安装依赖**

```bash
npm install neteasecloudmusicapi
```

- [ ] **Step 3: 创建启动文件 app.js**

```javascript
const { default: api } = require('neteasecloudmusicapi')

const PORT = 3000

// 启动服务
async function start() {
    const server = api.server

    server.listen(PORT, () => {
        console.log(`NeteaseCloudMusicApi running on http://localhost:${PORT}`)
    })
}

start().catch(console.error)
```

- [ ] **Step 4: 创建 .env 配置VIP账号**

```
NODE_ENV=production
PORT=3000
```

- [ ] **Step 5: 测试运行**

```bash
node app.js
```

预期输出：`NeteaseCloudMusicApi running on http://localhost:3000`

- [ ] **Step 6: 提交**

```bash
git init
git add .
git commit -m "feat: 初始化NeteaseCloudMusicApi中间层"
```

---

## 阶段二：Spring Boot 后端

### Task 2: Spring Boot 项目初始化

**Files:**
- Create: `server/pom.xml`
- Create: `server/src/main/resources/application.yml`
- Create: `server/src/main/java/com/music/MusicPlayerApplication.java`

- [ ] **Step 1: 创建项目结构**

```bash
mkdir -p server/src/main/java/com/music/{config,controller,service,mapper,entity,common}
mkdir -p server/src/main/resources/mapper
```

- [ ] **Step 2: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <groupId>com.music</groupId>
    <artifactId>music-player</artifactId>
    <version>1.0.0</version>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.5.5</version>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.3</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: 创建 application.yml**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/music_player?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.music.entity
  configuration:
    map-underscore-to-camel-case: true

jwt:
  secret: your-256-bit-secret-key-here-change-in-production
  expiration: 604800000  # 7 days in milliseconds

netease:
  api:
    base-url: http://localhost:3000
```

- [ ] **Step 4: 创建启动类**

```java
package com.music;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MusicPlayerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MusicPlayerApplication.class, args);
    }
}
```

- [ ] **Step 5: 验证项目编译**

```bash
cd server
mvn compile
```

预期输出：`BUILD SUCCESS`

- [ ] **Step 6: 提交**

```bash
git init
git add .
git commit -m "feat: 初始化Spring Boot后端项目"
```

---

### Task 3: 数据库表设计

**Files:**
- Create: `server/src/main/resources/schema.sql`

- [ ] **Step 1: 创建数据库Schema**

```sql
CREATE DATABASE IF NOT EXISTS music_player DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE music_player;

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE playlists (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    cover_url VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE playlist_songs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    playlist_id BIGINT NOT NULL,
    song_id VARCHAR(50) NOT NULL,
    position INT DEFAULT 0,
    added_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE favorites (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    song_id VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_song (user_id, song_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE song_cache (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    song_id VARCHAR(50) NOT NULL UNIQUE,
    url VARCHAR(500) NOT NULL,
    expire_at DATETIME NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_expire (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: 执行SQL**

```bash
mysql -u root -p < server/src/main/resources/schema.sql
```

- [ ] **Step 3: 提交**

```bash
git add .
git commit -m "feat: 添加数据库Schema"
```

---

### Task 4: 用户认证模块

**Files:**
- Create: `server/src/main/java/com/music/entity/User.java`
- Create: `server/src/main/java/com/music/mapper/UserMapper.java`
- Create: `server/src/main/java/com/music/service/UserService.java`
- Create: `server/src/main/java/com/music/controller/AuthController.java`
- Create: `server/src/main/java/com/music/common/Result.java`
- Create: `server/src/main/java/com/music/config/JwtConfig.java`
- Create: `server/src/main/java/com/music/common/JwtUtil.java`
- Create: `server/src/main/java/com/music/config/WebConfig.java`

- [ ] **Step 1: 创建统一响应类**

```java
package com.music.common;

public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // getters and setters
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
```

- [ ] **Step 2: 创建User实体**

```java
package com.music.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

- [ ] **Step 3: 创建UserMapper**

```java
package com.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

- [ ] **Step 4: 创建JWT工具类**

```java
package com.music.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {
    private static final SecretKey KEY = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);

    public static String generateToken(Long userId, String username) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 604800000))
                .signWith(KEY)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }
}
```

- [ ] **Step 5: 创建UserService**

```java
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
}
```

- [ ] **Step 6: 创建AuthController**

```java
package com.music.controller;

import com.music.common.Result;
import com.music.entity.User;
import com.music.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");
            return Result.success(userService.register(username, password));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        try {
            String username = params.get("username");
            String password = params.get("password");
            return Result.success(userService.login(username, password));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/me")
    public Result<User> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = com.music.common.JwtUtil.getUserId(token);
        return Result.success(userService.getUserById(userId));
    }
}
```

- [ ] **Step 7: 创建JWT拦截器配置**

```java
package com.music.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/register", "/api/auth/login");
    }
}
```

- [ ] **Step 8: 创建AuthInterceptor**

```java
package com.music.config;

import com.music.common.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }
        try {
            JwtUtil.parseToken(authHeader.replace("Bearer ", ""));
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }
}
```

- [ ] **Step 9: 验证JWT模块**

```bash
cd server
mvn compile
```

预期输出：`BUILD SUCCESS`

- [ ] **Step 10: 提交**

```bash
git add .
git commit -m "feat: 实现用户认证模块(JWT)"
```

---

### Task 5: 歌曲服务模块

**Files:**
- Create: `server/src/main/java/com/music/service/NeteaseApiService.java`
- Create: `server/src/main/java/com/music/controller/SongController.java`
- Create: `server/src/main/java/com/music/entity/SongCache.java`
- Create: `server/src/main/java/com/music/mapper/SongCacheMapper.java`

- [ ] **Step 1: 创建SongCache实体**

```java
package com.music.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("song_cache")
public class SongCache {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String songId;
    private String url;
    private LocalDateTime expireAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSongId() { return songId; }
    public void setSongId(String songId) { this.songId = songId; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

- [ ] **Step 2: 创建SongCacheMapper**

```java
package com.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.entity.SongCache;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SongCacheMapper extends BaseMapper<SongCache> {
}
```

- [ ] **Step 3: 创建NeteaseApiService**

```java
package com.music.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.music.entity.SongCache;
import com.music.mapper.SongCacheMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NeteaseApiService {
    private static final String NETEASE_API_BASE = "http://localhost:3000";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SongCacheMapper songCacheMapper;

    public String getSongUrl(String songId) {
        // 检查缓存
        SongCache cache = songCacheMapper.selectOne(
            new QueryWrapper<SongCache>().eq("song_id", songId)
        );
        if (cache != null && cache.getExpireAt().isAfter(LocalDateTime.now())) {
            return cache.getUrl();
        }

        // 调用Node API获取新URL
        try {
            String url = "https://music.163.com/song/media/outer/url?id=" + songId;
            String apiUrl = NETEASE_API_BASE + "/song/url?id=" + songId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            JsonNode json = objectMapper.readTree(response.body());
            JsonNode data = json.get("data");
            String finalUrl = data != null && !data.isEmpty() ? data.get(0).get("url").asText() : url;

            // 更新缓存
            if (cache != null) {
                cache.setUrl(finalUrl);
                cache.setExpireAt(LocalDateTime.now().plusHours(4));
                songCacheMapper.updateById(cache);
            } else {
                SongCache newCache = new SongCache();
                newCache.setSongId(songId);
                newCache.setUrl(finalUrl);
                newCache.setExpireAt(LocalDateTime.now().plusHours(4));
                songCacheMapper.insert(newCache);
            }
            return finalUrl;
        } catch (Exception e) {
            return "https://music.163.com/song/media/outer/url?id=" + songId;
        }
    }

    public String getLyric(String songId) {
        try {
            String apiUrl = NETEASE_API_BASE + "/lyric?id=" + songId;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            return response.body();
        } catch (Exception e) {
            return "{\"lrc\":\"\"}";
        }
    }

    public String search(String keyword) {
        try {
            String apiUrl = NETEASE_API_BASE + "/search?keywords=" + java.net.URLEncoder.encode(keyword, "UTF-8");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            return response.body();
        } catch (Exception e) {
            return "{\"result\":[]}";
        }
    }
}
```

- [ ] **Step 4: 创建SongController**

```java
package com.music.controller;

import com.music.common.Result;
import com.music.service.NeteaseApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/songs")
public class SongController {
    @Autowired
    private NeteaseApiService neteaseApiService;

    @GetMapping("/{id}/url")
    public Result<Map<String, String>> getSongUrl(@PathVariable String id) {
        String url = neteaseApiService.getSongUrl(id);
        return Result.success(Map.of("url", url));
    }

    @GetMapping("/{id}/lyric")
    public Result<String> getLyric(@PathVariable String id) {
        String lyric = neteaseApiService.getLyric(id);
        return Result.success(lyric);
    }

    @GetMapping("/search")
    public Result<String> search(@RequestParam String keyword) {
        String results = neteaseApiService.search(keyword);
        return Result.success(results);
    }
}
```

- [ ] **Step 5: 验证编译**

```bash
cd server
mvn compile
```

- [ ] **Step 6: 提交**

```bash
git add .
git commit -m "feat: 实现歌曲服务模块(URL/歌词/搜索)"
```

---

### Task 6: 歌单和收藏模块

**Files:**
- Create: `server/src/main/java/com/music/entity/Playlist.java`
- Create: `server/src/main/java/com/music/entity/PlaylistSong.java`
- Create: `server/src/main/java/com/music/entity/Favorite.java`
- Create: `server/src/main/java/com/music/mapper/PlaylistMapper.java`
- Create: `server/src/main/java/com/music/mapper/PlaylistSongMapper.java`
- Create: `server/src/main/java/com/music/mapper/FavoriteMapper.java`
- Create: `server/src/main/java/com/music/service/PlaylistService.java`
- Create: `server/src/main/java/com/music/service/FavoriteService.java`
- Create: `server/src/main/java/com/music/controller/PlaylistController.java`
- Create: `server/src/main/java/com/music/controller/FavoriteController.java`

- [ ] **Step 1: 创建实体类 (Playlist, PlaylistSong, Favorite)**

```java
package com.music.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("playlists")
public class Playlist {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String coverUrl;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

```java
package com.music.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("playlist_songs")
public class PlaylistSong {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long playlistId;
    private String songId;
    private Integer position;
    private LocalDateTime addedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPlaylistId() { return playlistId; }
    public void setPlaylistId(Long playlistId) { this.playlistId = playlistId; }
    public String getSongId() { return songId; }
    public void setSongId(String songId) { this.songId = songId; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}
```

```java
package com.music.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("favorites")
public class Favorite {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String songId;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getSongId() { return songId; }
    public void setSongId(String songId) { this.songId = songId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
```

- [ ] **Step 2: 创建Mapper接口**

```java
package com.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.entity.Playlist;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlaylistMapper extends BaseMapper<Playlist> {
}
```

```java
package com.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.entity.PlaylistSong;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlaylistSongMapper extends BaseMapper<PlaylistSong> {
}
```

```java
package com.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
```

- [ ] **Step 3: 创建PlaylistService**

```java
package com.music.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.music.entity.Playlist;
import com.music.entity.PlaylistSong;
import com.music.mapper.PlaylistMapper;
import com.music.mapper.PlaylistSongMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class PlaylistService {
    @Autowired
    private PlaylistMapper playlistMapper;
    @Autowired
    private PlaylistSongMapper playlistSongMapper;

    public List<Playlist> getUserPlaylists(Long userId) {
        return playlistMapper.selectList(new QueryWrapper<Playlist>().eq("user_id", userId));
    }

    public Playlist createPlaylist(Long userId, String name, String coverUrl) {
        Playlist playlist = new Playlist();
        playlist.setUserId(userId);
        playlist.setName(name);
        playlist.setCoverUrl(coverUrl);
        playlistMapper.insert(playlist);
        return playlist;
    }

    public Playlist getPlaylistById(Long playlistId) {
        return playlistMapper.selectById(playlistId);
    }

    public void updatePlaylist(Long playlistId, String name, String coverUrl) {
        Playlist playlist = playlistMapper.selectById(playlistId);
        if (playlist != null) {
            if (name != null) playlist.setName(name);
            if (coverUrl != null) playlist.setCoverUrl(coverUrl);
            playlistMapper.updateById(playlist);
        }
    }

    public void deletePlaylist(Long playlistId) {
        playlistMapper.deleteById(playlistId);
    }

    public void addSongToPlaylist(Long playlistId, String songId) {
        Integer maxPos = playlistSongMapper.selectCount(
            new QueryWrapper<PlaylistSong>().eq("playlist_id", playlistId)
        ).intValue();
        PlaylistSong ps = new PlaylistSong();
        ps.setPlaylistId(playlistId);
        ps.setSongId(songId);
        ps.setPosition(maxPos + 1);
        playlistSongMapper.insert(ps);
    }

    public void removeSongFromPlaylist(Long playlistId, String songId) {
        playlistSongMapper.delete(new QueryWrapper<PlaylistSong>()
            .eq("playlist_id", playlistId).eq("song_id", songId));
    }

    public List<PlaylistSong> getPlaylistSongs(Long playlistId) {
        return playlistSongMapper.selectList(
            new QueryWrapper<PlaylistSong>().eq("playlist_id", playlistId).orderByAsc("position")
        );
    }
}
```

- [ ] **Step 4: 创建FavoriteService**

```java
package com.music.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.music.entity.Favorite;
import com.music.mapper.FavoriteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FavoriteService {
    @Autowired
    private FavoriteMapper favoriteMapper;

    public List<Favorite> getUserFavorites(Long userId) {
        return favoriteMapper.selectList(new QueryWrapper<Favorite>().eq("user_id", userId));
    }

    public void addFavorite(Long userId, String songId) {
        if (favoriteMapper.selectCount(new QueryWrapper<Favorite>()
            .eq("user_id", userId).eq("song_id", songId)) == 0) {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setSongId(songId);
            favoriteMapper.insert(fav);
        }
    }

    public void removeFavorite(Long userId, String songId) {
        favoriteMapper.delete(new QueryWrapper<Favorite>()
            .eq("user_id", userId).eq("song_id", songId));
    }
}
```

- [ ] **Step 5: 创建PlaylistController**

```java
package com.music.controller;

import com.music.common.JwtUtil;
import com.music.common.Result;
import com.music.entity.Playlist;
import com.music.entity.PlaylistSong;
import com.music.service.PlaylistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {
    @Autowired
    private PlaylistService playlistService;

    @GetMapping
    public Result<List<Playlist>> getUserPlaylists(@RequestHeader("Authorization") String authHeader) {
        Long userId = JwtUtil.getUserId(authHeader.replace("Bearer ", ""));
        return Result.success(playlistService.getUserPlaylists(userId));
    }

    @PostMapping
    public Result<Playlist> createPlaylist(@RequestHeader("Authorization") String authHeader,
                                           @RequestBody Map<String, String> params) {
        Long userId = JwtUtil.getUserId(authHeader.replace("Bearer ", ""));
        return Result.success(playlistService.createPlaylist(userId, params.get("name"), params.get("coverUrl")));
    }

    @GetMapping("/{id}")
    public Result<Playlist> getPlaylist(@PathVariable Long id) {
        return Result.success(playlistService.getPlaylistById(id));
    }

    @PutMapping("/{id}")
    public Result<Void> updatePlaylist(@PathVariable Long id, @RequestBody Map<String, String> params) {
        playlistService.updatePlaylist(id, params.get("name"), params.get("coverUrl"));
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePlaylist(@PathVariable Long id) {
        playlistService.deletePlaylist(id);
        return Result.success(null);
    }

    @PostMapping("/{id}/songs")
    public Result<Void> addSong(@PathVariable Long id, @RequestBody Map<String, String> params) {
        playlistService.addSongToPlaylist(id, params.get("songId"));
        return Result.success(null);
    }

    @DeleteMapping("/{id}/songs/{songId}")
    public Result<Void> removeSong(@PathVariable Long id, @PathVariable String songId) {
        playlistService.removeSongFromPlaylist(id, songId);
        return Result.success(null);
    }

    @GetMapping("/{id}/songs")
    public Result<List<PlaylistSong>> getPlaylistSongs(@PathVariable Long id) {
        return Result.success(playlistService.getPlaylistSongs(id));
    }
}
```

- [ ] **Step 6: 创建FavoriteController**

```java
package com.music.controller;

import com.music.common.JwtUtil;
import com.music.common.Result;
import com.music.entity.Favorite;
import com.music.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
    @Autowired
    private FavoriteService favoriteService;

    @GetMapping
    public Result<List<Favorite>> getUserFavorites(@RequestHeader("Authorization") String authHeader) {
        Long userId = JwtUtil.getUserId(authHeader.replace("Bearer ", ""));
        return Result.success(favoriteService.getUserFavorites(userId));
    }

    @PostMapping
    public Result<Void> addFavorite(@RequestHeader("Authorization") String authHeader,
                                     @RequestBody Map<String, String> params) {
        Long userId = JwtUtil.getUserId(authHeader.replace("Bearer ", ""));
        favoriteService.addFavorite(userId, params.get("songId"));
        return Result.success(null);
    }

    @DeleteMapping("/{songId}")
    public Result<Void> removeFavorite(@RequestHeader("Authorization") String authHeader,
                                       @PathVariable String songId) {
        Long userId = JwtUtil.getUserId(authHeader.replace("Bearer ", ""));
        favoriteService.removeFavorite(userId, songId);
        return Result.success(null);
    }
}
```

- [ ] **Step 7: 验证编译**

```bash
cd server
mvn compile
```

- [ ] **Step 8: 提交**

```bash
git add .
git commit -m "feat: 实现歌单和收藏模块"
```

---

### Task 7: 后端启动验证

- [ ] **Step 1: 启动MySQL**

```bash
mysql -u root -p
```

- [ ] **Step 2: 执行schema.sql**

```bash
source server/src/main/resources/schema.sql
```

- [ ] **Step 3: 启动Spring Boot应用**

```bash
cd server
mvn spring-boot:run
```

预期输出：`Started MusicPlayerApplication in X seconds`

- [ ] **Step 4: 测试注册API**

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}'
```

预期输出：`{"code":200,"message":"success","data":{"token":"...","userId":1,"username":"test"}}`

- [ ] **Step 5: 提交**

```bash
git add .
git commit -m "test: 后端启动验证完成"
```

---

## 阶段三：Tauri + Vue3 客户端

### Task 8: Tauri 项目初始化

**Files:**
- Create: `client/package.json`
- Create: `client/vite.config.ts`
- Create: `client/src/main.ts`
- Create: `client/index.html`
- Create: `client/tauri.conf.json`

- [ ] **Step 1: 创建Vue3 + Vite项目**

```bash
cd "E:/music player"
npm create vite@latest client -- --template vue-ts
cd client
npm install
```

- [ ] **Step 2: 安装Tauri CLI并初始化**

```bash
npm install -D @tauri-apps/cli@latest
npx tauri init --app-name "music-player" --window-title "音乐播放器" --dev-url "http://localhost:5173" --before-dev-command "npm run dev" --before-build-command "npm run build"
```

- [ ] **Step 3: 安装Vue Router和Pinia**

```bash
npm install vue-router@4 pinia
```

- [ ] **Step 4: 创建tauri.conf.json配置**

```json
{
  "build": {
    "devtools": true
  },
  "app": {
    "windows": [
      {
        "title": "音乐播放器",
        "width": 1200,
        "height": 800,
        "minWidth": 900,
        "minHeight": 600,
        "resizable": true,
        "fullscreen": false
      }
    ]
  }
}
```

- [ ] **Step 5: 验证Tauri运行**

```bash
npx tauri dev
```

预期输出：窗口打开，显示Vue默认页面

- [ ] **Step 6: 提交**

```bash
git add .
git commit -m "feat: 初始化Tauri+Vue3客户端"
```

---

### Task 9: 前端核心组件开发

**Files:**
- Create: `client/src/stores/auth.ts`
- Create: `client/src/stores/player.ts`
- Create: `client/src/router/index.ts`
- Create: `client/src/components/Player.vue`
- Create: `client/src/components/SongList.vue`
- Create: `client/src/components/LyricDisplay.vue`
- Create: `client/src/components/PlaylistCard.vue`
- Create: `client/src/views/Login.vue`
- Create: `client/src/views/Register.vue`
- Create: `client/src/views/Home.vue`
- Create: `client/src/views/Search.vue`
- Create: `client/src/views/Playlist.vue`
- Create: `client/src/views/Favorites.vue`

- [ ] **Step 1: 创建Pinia Store - auth.ts**

```typescript
import { defineStore } from 'pinia'
import axios from 'axios'
import { ref } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref<number | null>(null)
  const username = ref('')

  const setAuth = (newToken: string, newUserId: number, newUsername: string) => {
    token.value = newToken
    userId.value = newUserId
    username.value = newUsername
    localStorage.setItem('token', newToken)
    axios.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
  }

  const logout = () => {
    token.value = ''
    userId.value = null
    username.value = ''
    localStorage.removeItem('token')
  }

  const initAuth = () => {
    const storedToken = localStorage.getItem('token')
    if (storedToken) {
      token.value = storedToken
      axios.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`
    }
  }

  return { token, userId, username, setAuth, logout, initAuth }
})
```

- [ ] **Step 2: 创建Pinia Store - player.ts**

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'

interface Song {
  id: string
  name: string
  artist: string
  album: string
  coverUrl: string
}

export const usePlayerStore = defineStore('player', () => {
  const currentSong = ref<Song | null>(null)
  const isPlaying = ref(false)
  const currentTime = ref(0)
  const duration = ref(0)
  const volume = ref(0.8)
  const playlist = ref<Song[]>([])
  const currentIndex = ref(-1)
  const lyricLines = ref<{ time: number; text: string }[]>([])
  const showLyric = ref(false)

  const play = (song: Song) => {
    currentSong.value = song
    isPlaying.value = true
  }

  const pause = () => {
    isPlaying.value = false
  }

  const togglePlay = () => {
    isPlaying.value = !isPlaying.value
  }

  const setCurrentTime = (time: number) => {
    currentTime.value = time
  }

  const setDuration = (dur: number) => {
    duration.value = dur
  }

  const setVolume = (vol: number) => {
    volume.value = vol
  }

  const playNext = () => {
    if (currentIndex.value < playlist.value.length - 1) {
      currentIndex.value++
      play(playlist.value[currentIndex.value])
    }
  }

  const playPrev = () => {
    if (currentIndex.value > 0) {
      currentIndex.value--
      play(playlist.value[currentIndex.value])
    }
  }

  return {
    currentSong, isPlaying, currentTime, duration, volume,
    playlist, currentIndex, lyricLines, showLyric,
    play, pause, togglePlay, setCurrentTime, setDuration, setVolume, playNext, playPrev
  }
})
```

- [ ] **Step 3: 创建Router**

```typescript
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('../views/Login.vue') },
    { path: '/register', component: () => import('../views/Register.vue') },
    { path: '/', component: () => import('../views/Home.vue') },
    { path: '/search', component: () => import('../views/Search.vue') },
    { path: '/playlist/:id', component: () => import('../views/Playlist.vue') },
    { path: '/favorites', component: () => import('../views/Favorites.vue') }
  ]
})

export default router
```

- [ ] **Step 4: 创建Player组件**

```vue
<template>
  <div class="player-bar">
    <div class="player-left">
      <img :src="currentSong?.coverUrl || '/default-cover.png'" class="cover" />
      <div class="song-info">
        <div class="song-name">{{ currentSong?.name || '未播放' }}</div>
        <div class="artist">{{ currentSong?.artist || '-' }}</div>
      </div>
    </div>
    <div class="player-center">
      <div class="controls">
        <button @click="playPrev">上一首</button>
        <button @click="togglePlay">{{ isPlaying ? '暂停' : '播放' }}</button>
        <button @click="playNext">下一首</button>
      </div>
      <div class="progress">
        <span>{{ formatTime(currentTime) }}</span>
        <input type="range" :value="currentTime" :max="duration" @input="seek" />
        <span>{{ formatTime(duration) }}</span>
      </div>
    </div>
    <div class="player-right">
      <button @click="showLyric = !showLyric">歌词</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { usePlayerStore } from '../stores/player'
import { storeToRefs } from 'pinia'

const player = usePlayerStore()
const { currentSong, isPlaying, currentTime, duration, showLyric } = storeToRefs(player)
const { togglePlay, playNext, playPrev, setCurrentTime } = player

const seek = (e: Event) => {
  const target = e.target as HTMLInputElement
  setCurrentTime(Number(target.value))
}

const formatTime = (seconds: number) => {
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}
</script>

<style scoped>
.player-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 80px;
  background: #333;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
}
.player-left { display: flex; align-items: center; }
.cover { width: 50px; height: 50px; }
.song-info { margin-left: 10px; }
.player-center { flex: 1; display: flex; flex-direction: column; align-items: center; }
.controls button { margin: 0 5px; }
.progress { display: flex; align-items: center; gap: 10px; }
</style>
```

- [ ] **Step 5: 创建SongList组件**

```vue
<template>
  <div class="song-list">
    <div v-for="song in songs" :key="song.id" class="song-item" @click="handleClick(song)">
      <img :src="song.coverUrl" class="song-cover" />
      <div class="song-detail">
        <div class="song-name">{{ song.name }}</div>
        <div class="artist">{{ song.artist }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Song {
  id: string
  name: string
  artist: string
  coverUrl: string
}

defineProps<{ songs: Song[] }>()
const emit = defineEmits<{ (e: 'play', song: Song): void }>()

const handleClick = (song: Song) => emit('play', song)
</script>

<style scoped>
.song-list { display: flex; flex-direction: column; gap: 10px; }
.song-item { display: flex; align-items: center; padding: 10px; cursor: pointer; }
.song-item:hover { background: #f5f5f5; }
.song-cover { width: 50px; height: 50px; }
.song-detail { margin-left: 10px; }
</style>
```

- [ ] **Step 6: 创建LyricDisplay组件**

```vue
<template>
  <div class="lyric-panel" :class="{ open: showLyric }">
    <div class="lyric-content">
      <div v-for="(line, index) in lyricLines" :key="index"
           class="lyric-line"
           :class="{ active: index === currentLineIndex }">
        {{ line.text }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { usePlayerStore } from '../stores/player'
import { storeToRefs } from 'pinia'

const player = usePlayerStore()
const { lyricLines, currentTime, showLyric } = storeToRefs(player)

const currentLineIndex = computed(() => {
  for (let i = lyricLines.value.length - 1; i >= 0; i--) {
    if (currentTime.value >= lyricLines.value[i].time) return i
  }
  return 0
})
</script>

<style scoped>
.lyric-panel {
  position: fixed;
  right: -400px;
  top: 0;
  bottom: 80px;
  width: 400px;
  background: #fff;
  box-shadow: -2px 0 10px rgba(0,0,0,0.1);
  transition: right 0.3s;
  overflow-y: auto;
}
.lyric-panel.open { right: 0; }
.lyric-content { padding: 20px; }
.lyric-line { padding: 10px 0; font-size: 16px; }
.lyric-line.active { color: #ec4141; font-weight: bold; }
</style>
```

- [ ] **Step 7: 创建Home页面**

```vue
<template>
  <div class="home">
    <h1>发现音乐</h1>
    <div class="recommend-section">
      <h2>推荐歌曲</h2>
      <SongList :songs="songs" @play="playSong" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import SongList from '../components/SongList.vue'
import { usePlayerStore } from '../stores/player'

const player = usePlayerStore()
const songs = ref<any[]>([])

const playSong = (song: any) => {
  player.play(song)
  fetchSongUrl(song.id)
}

const fetchSongUrl = async (songId: string) => {
  const res = await axios.get(`/api/songs/${songId}/url`)
  console.log('Song URL:', res.data.data.url)
}

onMounted(async () => {
  const res = await axios.get('/api/songs/search?keyword=周杰伦')
  const data = JSON.parse(res.data.data)
  songs.value = data.result.songs?.slice(0, 10).map((s: any) => ({
    id: s.id,
    name: s.name,
    artist: s.artists?.[0]?.name || '未知',
    coverUrl: s.album?.picUrl || '/default-cover.png'
  })) || []
})
</script>

<style scoped>
.home { padding: 20px; }
</style>
```

- [ ] **Step 8: 创建Login页面**

```vue
<template>
  <div class="login-page">
    <h1>登录</h1>
    <form @submit.prevent="handleLogin">
      <input v-model="username" placeholder="用户名" required />
      <input v-model="password" type="password" placeholder="密码" required />
      <button type="submit">登录</button>
    </form>
    <p>还没有账号？<router-link to="/register">注册</router-link></p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const username = ref('')
const password = ref('')
const router = useRouter()
const auth = useAuthStore()

const handleLogin = async () => {
  const res = await axios.post('/api/auth/login', { username: username.value, password: password.value })
  auth.setAuth(res.data.data.token, res.data.data.userId, res.data.data.username)
  router.push('/')
}
</script>

<style scoped>
.login-page { display: flex; flex-direction: column; align-items: center; padding: 50px; }
input { margin: 10px 0; padding: 10px; width: 300px; }
button { padding: 10px 30px; background: #ec4141; color: white; border: none; cursor: pointer; }
</style>
```

- [ ] **Step 9: 创建Register页面**

```vue
<template>
  <div class="register-page">
    <h1>注册</h1>
    <form @submit.prevent="handleRegister">
      <input v-model="username" placeholder="用户名" required />
      <input v-model="password" type="password" placeholder="密码" required />
      <button type="submit">注册</button>
    </form>
    <p>已有账号？<router-link to="/login">登录</router-link></p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const username = ref('')
const password = ref('')
const router = useRouter()
const auth = useAuthStore()

const handleRegister = async () => {
  const res = await axios.post('/api/auth/register', { username: username.value, password: password.value })
  auth.setAuth(res.data.data.token, res.data.data.userId, res.data.data.username)
  router.push('/')
}
</script>

<style scoped>
.register-page { display: flex; flex-direction: column; align-items: center; padding: 50px; }
input { margin: 10px 0; padding: 10px; width: 300px; }
button { padding: 10px 30px; background: #ec4141; color: white; border: none; cursor: pointer; }
</style>
```

- [ ] **Step 10: 创建Search页面**

```vue
<template>
  <div class="search-page">
    <input v-model="keyword" placeholder="搜索歌曲/歌手" @keyup.enter="search" />
    <button @click="search">搜索</button>
    <SongList :songs="songs" @play="playSong" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import SongList from '../components/SongList.vue'
import { usePlayerStore } from '../stores/player'

const keyword = ref('')
const songs = ref<any[]>([])
const player = usePlayerStore()

const search = async () => {
  const res = await axios.get(`/api/songs/search?keyword=${keyword.value}`)
  const data = JSON.parse(res.data.data)
  songs.value = data.result.songs?.slice(0, 20).map((s: any) => ({
    id: s.id,
    name: s.name,
    artist: s.artists?.[0]?.name || '未知',
    coverUrl: s.album?.picUrl || '/default-cover.png'
  })) || []
}

const playSong = (song: any) => player.play(song)
</script>

<style scoped>
.search-page { padding: 20px; }
input { padding: 10px; width: 300px; }
button { padding: 10px 20px; margin-left: 10px; }
</style>
```

- [ ] **Step 11: 创建Favorites页面**

```vue
<template>
  <div class="favorites-page">
    <h1>我的收藏</h1>
    <SongList :songs="songs" @play="playSong" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import SongList from '../components/SongList.vue'
import { usePlayerStore } from '../stores/player'

const songs = ref<any[]>([])
const player = usePlayerStore()

onMounted(async () => {
  const res = await axios.get('/api/favorites')
  const favorites = res.data.data
  songs.value = favorites.map((f: any) => ({
    id: f.songId,
    name: '歌曲',
    artist: '未知',
    coverUrl: '/default-cover.png'
  }))
})

const playSong = (song: any) => player.play(song)
</script>

<style scoped>
.favorites-page { padding: 20px; }
</style>
```

- [ ] **Step 12: 创建Playlist页面**

```vue
<template>
  <div class="playlist-page">
    <h1>{{ playlistName }}</h1>
    <button @click="showAddSong = true">添加歌曲</button>
    <SongList :songs="songs" @play="playSong" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import SongList from '../components/SongList.vue'
import { usePlayerStore } from '../stores/player'

const route = useRoute()
const playlistName = ref('')
const songs = ref<any[]>([])
const player = usePlayerStore()

onMounted(async () => {
  const id = route.params.id
  const plRes = await axios.get(`/api/playlists/${id}`)
  playlistName.value = plRes.data.data.name

  const songsRes = await axios.get(`/api/playlists/${id}/songs`)
  songs.value = songsRes.data.data.map((s: any) => ({
    id: s.songId,
    name: '歌曲',
    artist: '未知',
    coverUrl: '/default-cover.png'
  }))
})

const playSong = (song: any) => player.play(song)
</script>

<style scoped>
.playlist-page { padding: 20px; }
</style>
```

- [ ] **Step 13: 更新App.vue**

```vue
<template>
  <div id="app">
    <router-view />
    <Player />
    <LyricDisplay />
  </div>
</template>

<script setup lang="ts">
import Player from './components/Player.vue'
import LyricDisplay from './components/LyricDisplay.vue'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
auth.initAuth()
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
</style>
```

- [ ] **Step 14: 更新main.ts**

```typescript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
```

- [ ] **Step 15: 验证编译**

```bash
cd client
npm run build
```

预期输出：`built in Xs`

- [ ] **Step 16: 提交**

```bash
git add .
git commit -m "feat: 实现前端核心组件和页面"
```

---

## 阶段四：集成测试

### Task 10: 端到端测试

- [ ] **Step 1: 确保所有服务运行**

```bash
# Terminal 1: MySQL
mysql -u root -p

# Terminal 2: NeteaseCloudMusicApi
cd netease-api && node app.js

# Terminal 3: Spring Boot
cd server && mvn spring-boot:run

# Terminal 4: Tauri Client
cd client && npx tauri dev
```

- [ ] **Step 2: 测试用户注册/登录流程**

在客户端注册新账号，验证登录后能访问受保护的API

- [ ] **Step 3: 测试歌曲搜索和播放**

搜索歌曲，点击播放，验证音频能否正常播放

- [ ] **Step 4: 测试歌单创建和收藏功能**

创建歌单，添加歌曲到歌单，收藏歌曲

- [ ] **Step 5: 测试歌词显示**

播放有歌词的歌曲，验证歌词同步显示

- [ ] **Step 6: 提交最终版本**

```bash
git add .
git commit -m "feat: 完成音乐播放器端到端集成"
```

---

## 技术验证清单

- [ ] MySQL数据库连接正常，5张表创建成功
- [ ] Spring Boot后端启动成功，端口8080
- [ ] NeteaseCloudMusicApi(Node.js)启动成功，端口3000
- [ ] 用户注册/登录API正常工作
- [ ] 歌曲URL获取和缓存正常工作
- [ ] Tauri客户端成功构建并运行
- [ ] JWT认证在所有受保护路由正常工作
- [ ] 播放器能播放歌曲并显示歌词