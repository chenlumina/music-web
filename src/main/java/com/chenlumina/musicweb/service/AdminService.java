package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.AdminDao;
import com.chenlumina.musicweb.dao.PlaylistDao;
import com.chenlumina.musicweb.entity.Playlist;

import java.util.List;
import java.util.Map;

/** 后台业务：统计数据与全量歌单 */
public class AdminService {

    private final AdminDao adminDao = new AdminDao();
    private final PlaylistDao playlistDao = new PlaylistDao();

    public Map<String, Long> stats() {
        return adminDao.stats();
    }

    public List<Playlist> allPlaylists() {
        return playlistDao.findAll();
    }
}