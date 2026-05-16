package com.music.controller;

import com.music.common.Result;
import com.music.service.NeteaseApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/songs")
public class SongController {
    @Autowired
    private NeteaseApiService neteaseApiService;

    @GetMapping("/{id}/url")
    public Result<Map<String, Object>> getSongUrl(@PathVariable String id) {
        Map<String, Object> result = neteaseApiService.getSongUrlWithPreviewStatus(id, null);
        Map<String, Object> response = new HashMap<>();
        response.put("url", result.get("url"));
        response.put("isPreview", result.get("isPreview"));
        response.put("cookieExpired", result.get("cookieExpired"));
        return Result.success(response);
    }

    @GetMapping("/{id}/lyric")
    public Result<String> getLyric(@PathVariable String id) {
        String lyric = neteaseApiService.getLyric(id);
        return Result.success(lyric);
    }

    @PostMapping("/search")
    public Result<String> search(@RequestBody Map<String, String> body) {
        String keyword = body.get("keyword");
        String results = neteaseApiService.search(keyword);
        return Result.success(results);
    }

    @GetMapping("/details")
    public Result<Map<String, String>> getSongDetails(@RequestParam String ids) {
        Map<String, String> coverMap = neteaseApiService.getSongCovers(ids);
        return Result.success(coverMap);
    }

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
}