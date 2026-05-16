# Music Player - 音乐播放器

## 项目概述

类网易云音乐界面的桌面音乐播放器，使用Tauri+Vue3构建客户端，Spring Boot+MySQL构建后端，通过NeteaseCloudMusicApi调用用户VIP账号获取歌曲资源。

## 技术栈

| 层级 | 技术 |
|------|------|
| 客户端 | Tauri 2.x + Vue 3 + TypeScript + Pinia + Vue Router |
| 后端 | Spring Boot 3.x + Java 17 + MyBatis-Plus + JWT |
| 数据库 | MySQL 8.x |
| 中间层 | NeteaseCloudMusicApi (Node.js) |

## 项目结构

```
music-player/
├── client/                    # Tauri + Vue3 前端
│   ├── src/
│   │   ├── views/            # 页面组件
│   │   ├── components/       # 公共组件
│   │   ├── stores/           # Pinia 状态管理
│   │   └── router/           # Vue Router
│   └── tauri.conf.json
├── server/                    # Spring Boot 后端
│   ├── src/main/java/com/music/
│   │   ├── config/           # 配置类
│   │   ├── controller/      # REST API 控制器
│   │   ├── service/         # 业务逻辑
│   │   ├── mapper/          # MyBatis Mapper
│   │   ├── entity/          # 实体类
│   │   └── common/          # 公共工具类
│   └── src/main/resources/
└── netease-api/              # NeteaseCloudMusicApi (Node.js)
```

## 快速启动

### 前置条件

- Node.js 18+
- JDK 17+
- Maven 3.8+
- MySQL 8.x

### 配置

1. 编辑 `server/src/main/resources/application.yml`，设置MySQL密码为 `123456`
2. 创建数据库：
```bash
mysql -u root -p123456 -e "CREATE DATABASE IF NOT EXISTS music_player"
```
3. 执行 schema.sql 创建表：
```bash
mysql -u root -p123456 music_player < server/src/main/resources/schema.sql
```

### 启动顺序

```bash
# Terminal 1: NeteaseCloudMusicApi (端口 3000)
cd netease-api && node app.js

# Terminal 2: Spring Boot 后端 (端口 8080)
cd server && mvn spring-boot:run

# Terminal 3: Tauri 客户端
cd client && npx tauri dev
```

## API 端点

### 认证
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/me` - 获取当前用户信息

### 歌曲
- `GET /api/songs/{id}/url` - 获取歌曲播放URL（带缓存）
- `GET /api/songs/{id}/lyric` - 获取歌词
- `GET /api/songs/search?keyword=xxx` - 搜索歌曲

### 歌单
- `GET /api/playlists` - 获取用户歌单列表
- `POST /api/playlists` - 创建歌单
- `GET /api/playlists/{id}` - 获取歌单详情
- `POST /api/playlists/{id}/songs` - 添加歌曲到歌单

### 收藏
- `GET /api/favorites` - 获取收藏列表
- `POST /api/favorites` - 添加收藏
- `DELETE /api/favorites/{songId}` - 取消收藏

## 开发注意事项

- 后端使用 JWT 认证，token 有效期 7 天
- 歌曲 URL 缓存在 MySQL 中，有效期 4 小时
- 前端 API 请求需要携带 `Authorization: Bearer <token>` 头
- 前端 API 基础路径配置在各个 View 组件的 axios 调用中（当前为 `http://localhost:8080`）