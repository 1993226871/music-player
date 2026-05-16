# VIP歌曲下载播放实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 对于VIP歌曲，后端下载到本地后流式传输给前端播放，绕过CDN访问限制

**Architecture:** 后端实现下载端点从CDN获取歌曲并保存到本地磁盘，同时提供流式播放端点。前端通过后端代理播放歌曲，而非直接访问CDN URL。

**Tech Stack:** Spring Boot, Java 17, Vue3, Tauri

---

## 文件结构

```
server/
├── src/main/java/com/music/
│   ├── controller/SongController.java      # 新增download/stream端点
│   ├── service/NeteaseApiService.java     # 新增下载方法
│   └── config/WebConfig.java               # 添加/static/songs/**路径排除
├── src/main/resources/
│   └── application.yml                     # 配置歌曲存储路径
client/
├── src/components/Player.vue               # 修改为使用流式端点
└── src/stores/player.ts                   # 可能需要修改
```

---

### Task 1: 配置歌曲存储路径

**Files:**
- Modify: `server/src/main/resources/application.yml`

- [ ] **Step 1: 添加歌曲存储目录配置**

```yaml
# 在server/src/main/resources/application.yml末尾添加
storage:
  songs:
    path: ${user.home}/.music-player/songs
    cleanup:
      enabled: true
      max-age-hours: 24
```

- [ ] **Step 2: 验证配置生效**

Run: `cd server && JAVA_HOME="D:/JDK 17" mvn compile -q`
Expected: 编译成功

- [ ] **Step 3: 提交**

```bash
git add server/src/main/resources/application.yml
git commit -m "feat: add song storage path config"
```

---

### Task 2: 实现NeteaseApiService下载方法

**Files:**
- Modify: `server/src/main/java/com/music/service/NeteaseApiService.java`

- [ ] **Step 1: 添加import语句**

在文件开头添加:
```java
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
```

- [ ] **Step 2: 添加存储路径配置字段**

在类的开头添加:
```java
@Value("${storage.songs.path}")
private String songsStoragePath;
```

- [ ] **Step 3: 添加下载歌曲方法**

在类末尾添加方法:
```java
public String downloadSong(String songId) {
    // 检查是否已下载
    String localPath = songsStoragePath + "/" + songId + ".mp3";
    Path path = Paths.get(localPath);
    if (Files.exists(path)) {
        return localPath;
    }

    // 获取CDN URL
    String cdnUrl = getSongUrl(songId);
    if (cdnUrl == null || cdnUrl.contains("music.163.com/song/media/outer")) {
        return null; // 无法获取有效URL
    }

    // 下载到本地
    try {
        Files.createDirectories(path.getParent());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(cdnUrl))
                .GET()
                .build();
        HttpResponse<InputStream> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() == 200) {
            Files.copy(response.body(), path, StandardCopyOption.REPLACE_EXISTING);
            return localPath;
        }
    } catch (Exception e) {
        System.err.println("Failed to download song " + songId + ": " + e.getMessage());
    }
    return null;
}

public String getLocalSongPath(String songId) {
    String localPath = songsStoragePath + "/" + songId + ".mp3";
    if (Files.exists(Paths.get(localPath))) {
        return localPath;
    }
    return null;
}
```

- [ ] **Step 4: 验证编译**

Run: `JAVA_HOME="D:/JDK 17" mvn compile -f "E:/music player/server/pom.xml" -q`
Expected: 编译成功

- [ ] **Step 5: 提交**

```bash
git add server/src/main/java/com/music/service/NeteaseApiService.java
git commit -m "feat: add downloadSong method to NeteaseApiService"
```

---

### Task 3: 实现SongController下载和流式播放端点

**Files:**
- Modify: `server/src/main/java/com/music/controller/SongController.java`

- [ ] **Step 1: 添加新的import**

```java
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.File;
```

- [ ] **Step 2: 添加download端点**

在现有端点后添加:
```java
@GetMapping("/{id}/download")
public Result<Map<String, Object>> downloadSong(@PathVariable String id) {
    String localPath = neteaseApiService.downloadSong(id);
    if (localPath != null) {
        return Result.success(Map.of(
            "path", localPath,
            "downloaded", true
        ));
    }
    return Result.error("Failed to download song");
}

@GetMapping("/{id}/stream")
public ResponseEntity<Resource> streamSong(@PathVariable String id) {
    // 先尝试本地文件
    String localPath = neteaseApiService.getLocalSongPath(id);
    if (localPath == null) {
        // 下载后播放
        localPath = neteaseApiService.downloadSong(id);
    }
    
    if (localPath == null) {
        return ResponseEntity.notFound().build();
    }

    Resource resource = new FileSystemResource(localPath);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
    headers.add("Accept-Ranges", "bytes");
    
    return ResponseEntity.ok()
            .headers(headers)
            .contentLength(new File(localPath).length())
            .body(resource);
}
```

- [ ] **Step 3: 添加download端点到认证排除列表**

修改WebConfig.java的excludePathPatterns，添加`/api/songs/*/download`和`/api/songs/*/stream`

- [ ] **Step 4: 验证编译**

Run: `JAVA_HOME="D:/JDK 17" mvn compile -f "E:/music player/server/pom.xml" -q`
Expected: 编译成功

- [ ] **Step 5: 提交**

```bash
git add server/src/main/java/com/music/controller/SongController.java
git commit -m "feat: add download and stream endpoints"
```

---

### Task 4: 修改Player.vue使用流式端点

**Files:**
- Modify: `client/src/components/Player.vue`

- [ ] **Step 1: 修改获取歌曲URL的逻辑**

找到watch(currentSong, ...)中的fetch逻辑，修改为:
```javascript
// Watch for song changes to load and play
watch(currentSong, async (song) => {
  if (!song || !audioEl.value) return

  // Reset state
  player.setDuration(0)
  player.setCurrentTime(0)

  // Get song URL - use stream endpoint for VIP songs
  try {
    const token = localStorage.getItem('token')
    const headers = token ? { 'Authorization': `Bearer ${token}` } : {}
    
    // 先尝试直接URL，如果失败再用流式端点
    const res = await fetch(`/api/songs/${song.id}/url`, { headers })
    const data = await res.json()
    
    if (data.data?.url) {
      // 使用直接URL
      audioEl.value.src = data.data.url
    } else {
      // 使用流式端点
      audioEl.value.src = `/api/songs/${song.id}/stream`
    }
    audioEl.value.load()
    player.setIsPlaying(true)
  } catch (e) {
    console.error('Failed to get song URL:', e)
    // 备用方案：使用流式端点
    audioEl.value.src = `/api/songs/${song.id}/stream`
    audioEl.value.load()
    player.setIsPlaying(true)
  }
})
```

- [ ] **Step 5: 提交**

```bash
git add client/src/components/Player.vue
git commit -m "feat: use stream endpoint for VIP songs"
```

---

### Task 5: 测试完整流程

- [ ] **Step 1: 重启所有服务**

```bash
# Terminal 1: 重启 netease-api
taskkill //F //PID $(netstat -ano | grep 3000 | grep LISTEN | awk '{print $5}')
cd netease-api && node app.js

# Terminal 2: 重启 Spring Boot
taskkill //F //PID $(netstat -ano | grep 8080 | grep LISTEN | awk '{print $5}')
JAVA_HOME="D:/JDK 17" mvn spring-boot:run -f server/pom.xml

# Terminal 3: Vite (如果需要)
cd client && npm run dev
```

- [ ] **Step 2: 搜索一首VIP歌曲（如蔡依林-大艺术家）并播放**

验证控制台输出，确认使用的是stream端点

- [ ] **Step 3: 验证播放进度条可以拖动，播放时长正确**

---

## 执行选项

**Plan complete and saved to `docs/superpowers/plans/2026-05-16-vip-song-streaming.md`. Two execution options:**

**1. Subagent-Driven (recommended)** - I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** - Execute tasks in this session using executing-plans, batch execution with checkpoints

**Which approach?**