package com.music.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.music.entity.SongCache;
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

            // 修复URL中的特殊字符编码问题
            if (finalUrl != null && finalUrl.contains("vuutv=")) {
                int vuutvIndex = finalUrl.indexOf("vuutv=");
                String beforeVuutv = finalUrl.substring(0, vuutvIndex + 6);
                String vuutvValue = finalUrl.substring(vuutvIndex + 6);
                // 确保 + 被正确编码为 %2B
                vuutvValue = vuutvValue.replace("+", "%2B");
                finalUrl = beforeVuutv + vuutvValue;
            }

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