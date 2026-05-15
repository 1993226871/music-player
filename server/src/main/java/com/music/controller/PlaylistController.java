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
