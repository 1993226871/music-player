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