package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.TagDao;
import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.entity.Tag;

import java.util.List;

/** 标签业务：字典读取、歌单标签分配、按标签筛选 */
public class TagService {

    private final TagDao tagDao = new TagDao();

    public List<Tag> listAll() {
        return tagDao.findAll();
    }

    public List<Tag> tagsOf(long playlistId) {
        return tagDao.findByPlaylist(playlistId);
    }

    public List<Long> idsOf(long playlistId) {
        return tagDao.findIdsByPlaylist(playlistId);
    }

    public void assign(long playlistId, List<Long> tagIds) {
        tagDao.assign(playlistId, tagIds);
    }

    public List<Playlist> listOnlineByTag(long tagId) {
        return tagDao.findOnlineByTag(tagId);
    }
}