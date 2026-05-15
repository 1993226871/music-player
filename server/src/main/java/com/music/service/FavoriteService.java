package com.music.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.music.entity.Favorite;
import com.music.mapper.FavoriteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FavoriteService {
    @Autowired
    private FavoriteMapper favoriteMapper;

    public List<Favorite> getUserFavorites(Long userId) {
        return favoriteMapper.selectList(new QueryWrapper<Favorite>().eq("user_id", userId));
    }

    public void addFavorite(Long userId, String songId) {
        if (favoriteMapper.selectCount(new QueryWrapper<Favorite>()
            .eq("user_id", userId).eq("song_id", songId)) == 0) {
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setSongId(songId);
            favoriteMapper.insert(fav);
        }
    }

    public void removeFavorite(Long userId, String songId) {
        favoriteMapper.delete(new QueryWrapper<Favorite>()
            .eq("user_id", userId).eq("song_id", songId));
    }
}
