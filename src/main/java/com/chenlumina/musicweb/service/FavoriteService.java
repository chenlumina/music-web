package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.FavoriteDao;
import com.chenlumina.musicweb.dao.PlaylistDao;
import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.util.ValidateException;

import java.util.List;

/** 收藏业务：收藏/取消收藏切换，歌单必须存在 */
public class FavoriteService {

    private final FavoriteDao favoriteDao = new FavoriteDao();
    private final PlaylistDao playlistDao = new PlaylistDao();

    public boolean isFavorited(Long userId, long playlistId) {
        return userId != null && favoriteDao.exists(userId, playlistId);
    }

    /** 切换收藏状态，返回切换后是否已收藏 */
    public boolean toggle(long userId, long playlistId) {
        if (playlistDao.findById(playlistId) == null) {
            throw new ValidateException("歌单不存在");
        }
        if (favoriteDao.exists(userId, playlistId)) {
            favoriteDao.remove(userId, playlistId);
            return false;
        }
        favoriteDao.add(userId, playlistId);
        return true;
    }

    public List<Playlist> listMine(long userId) {
        return favoriteDao.listByUser(userId);
    }
}