package com.music.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.music.entity.SongCache;
import com.music.entity.User;
import com.music.mapper.SongCacheMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;

@Service
public class NeteaseApiService {
    @Value("${storage.songs.path}")
    private String songsStoragePath;

    private static final String NETEASE_API_BASE = "http://localhost:3000";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 记录Cookie是否已过期
    private volatile boolean cookieExpired = false;

    @Autowired
    private SongCacheMapper songCacheMapper;

    @Autowired
    private UserService userService;

    public String getSongUrl(String songId) {
        return getSongUrlWithPreviewStatus(songId, null).get("url").toString();
    }

    public void resetCookieExpired() {
        this.cookieExpired = false;
    }

    // 验证cookie是否有效
    public Map<String, Object> validateCookie(String cookie) {
        Map<String, Object> result = new HashMap<>();
        result.put("valid", false);
        result.put("message", "");

        try {
            // 用一首VIP歌曲测试，林俊杰的《江南》需要VIP
            String apiUrl = NETEASE_API_BASE + "/song/url/v1?id=108914&level=sky";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Cookie", cookie)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = objectMapper.readTree(response.body());
            JsonNode data = json.get("data");

            if (data != null && data.isArray() && data.size() > 0) {
                JsonNode songData = data.get(0);
                Integer time = songData.has("time") ? songData.get("time").asInt() : null;
                Integer payed = songData.has("payed") ? songData.get("payed").asInt() : null;

                // 有效的VIP cookie特征：payed=1 或者 time>180000 (完整版非预览)
                if (payed != null && payed == 1) {
                    result.put("valid", true);
                    result.put("message", "Cookie有效，可播放VIP完整歌曲");
                } else if (time != null && time > 180000) {
                    result.put("valid", true);
                    result.put("message", "Cookie有效，可播放完整歌曲");
                } else if (time != null && time == 45035) {
                    result.put("valid", false);
                    result.put("message", "Cookie已过期或无效，仅能播放预览版");
                } else {
                    result.put("valid", false);
                    result.put("message", "Cookie无效(请确认是VIP付费账号)");
                }
            } else {
                result.put("valid", false);
                result.put("message", "无法获取歌曲信息");
            }
        } catch (Exception e) {
            result.put("valid", false);
            result.put("message", "验证失败: " + e.getMessage());
        }
        return result;
    }

    // 从数据库获取admin用户的cookie
    private String getAdminCookie() {
        return userService.getAdminCookie();
    }

    // 返回URL和是否预览的Map
    public Map<String, Object> getSongUrlWithPreviewStatus(String songId, String cookie) {
        Map<String, Object> result = new HashMap<>();
        result.put("url", "");
        result.put("isPreview", false);
        result.put("cookieExpired", false);

        // 检查缓存
        SongCache cache = songCacheMapper.selectOne(
            new QueryWrapper<SongCache>().eq("song_id", songId)
        );
        if (cache != null && cache.getExpireAt().isAfter(LocalDateTime.now())) {
            result.put("url", cache.getUrl());
            result.put("isPreview", false); // 缓存的URL认为不是预览
            return result;
        }

        // 获取cookie：优先使用传入的cookie，否则使用admin用户的cookie
        String actualCookie = cookie != null ? cookie : getAdminCookie();

        // 调用Node API获取新URL
        try {
            String apiUrl = NETEASE_API_BASE + "/song/url/v1?id=" + songId + "&level=sky";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Cookie", actualCookie)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            JsonNode json = objectMapper.readTree(response.body());
            JsonNode data = json.get("data");
            String finalUrl = null;
            boolean isPreview = false;

            if (data != null && data.isArray() && data.size() > 0) {
                JsonNode songData = data.get(0);
                finalUrl = songData.get("url").asText();
                Integer time = songData.has("time") ? songData.get("time").asInt() : null;

                if (time != null && time < 60000) {
                    isPreview = true;
                    // 如果Cookie过期，time会是45035（45秒预览）
                    // 正常VIP歌曲的time应该是歌曲时长（如197500ms）
                    if (time == 45035 && !cookieExpired) {
                        // 首次检测到Cookie可能过期，标记
                        cookieExpired = true;
                        result.put("cookieExpired", true);
                    }
                }
            }

            if (finalUrl == null) {
                finalUrl = "https://music.163.com/song/media/outer/url?id=" + songId;
            } else {
                // 修复URL中的特殊字符编码问题
                if (finalUrl.contains("vuutv=")) {
                    int vuutvIndex = finalUrl.indexOf("vuutv=");
                    String beforeVuutv = finalUrl.substring(0, vuutvIndex + 6);
                    String vuutvValue = finalUrl.substring(vuutvIndex + 6);
                    vuutvValue = vuutvValue.replace("+", "%2B");
                    finalUrl = beforeVuutv + vuutvValue;
                }
            }

            result.put("url", finalUrl);
            result.put("isPreview", isPreview);

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

            return result;
        } catch (Exception e) {
            result.put("url", "https://music.163.com/song/media/outer/url?id=" + songId);
            return result;
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
            // Do NOT encode - Spring @RequestParam already decodes URL parameters
            String apiUrl = NETEASE_API_BASE + "/search?keywords=" + keyword;
            System.out.println("Searching netease API with URL: " + apiUrl);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            System.out.println("Netease API response: " + response.body());
            return response.body();
        } catch (Exception e) {
            System.err.println("Search error: " + e.getMessage());
            e.printStackTrace();
            return "{\"result\":[]}";
        }
    }

    public Map<String, String> getSongCovers(String ids) {
        Map<String, String> coverMap = new HashMap<>();
        try {
            String apiUrl = NETEASE_API_BASE + "/song/detail?ids=" + ids;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

            JsonNode json = objectMapper.readTree(response.body());
            JsonNode songs = json.get("songs");
            if (songs != null && songs.isArray()) {
                for (JsonNode song : songs) {
                    String id = song.get("id").asText();
                    JsonNode al = song.get("al");
                    String picUrl = al != null ? al.get("picUrl").asText() : "";
                    coverMap.put(id, picUrl);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch song covers: " + e.getMessage());
        }
        return coverMap;
    }

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
}