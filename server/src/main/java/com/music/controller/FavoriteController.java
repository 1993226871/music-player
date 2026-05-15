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
