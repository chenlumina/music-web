package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.PlaylistDao;
import com.chenlumina.musicweb.dao.RecommendDao;
import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.util.ValidateException;

import java.util.List;

/** 推荐业务：歌单必须存在；推荐仅对前台可见的上架歌单展示 */
public class RecommendService {

    private final RecommendDao recommendDao = new RecommendDao();
    private final PlaylistDao playlistDao = new PlaylistDao();

    public void add(long playlistId) {
        if (playlistDao.findById(playlistId) == null) {
            throw new ValidateException("歌单不存在");
        }
        if (!recommendDao.exists(playlistId)) {
            recommendDao.add(playlistId);
        }
    }

    public void remove(long playlistId) {
        recommendDao.remove(playlistId);
    }

    public List<Playlist> listOnline() {
        return recommendDao.findOnlineRecommended();
    }

    public List<Long> ids() {
        return recommendDao.findIds();
    }
}