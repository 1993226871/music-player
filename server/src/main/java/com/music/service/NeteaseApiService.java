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