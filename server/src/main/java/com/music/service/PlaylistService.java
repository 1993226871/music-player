package com.music.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.music.entity.Playlist;
import com.music.entity.PlaylistSong;
import com.music.mapper.PlaylistMapper;
import com.music.mapper.PlaylistSongMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

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
