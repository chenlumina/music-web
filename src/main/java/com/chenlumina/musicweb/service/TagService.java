package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.TagDao;
import com.chenlumina.musicweb.dto.TagDto;
import com.chenlumina.musicweb.entity.Tag;
import com.chenlumina.musicweb.util.DbUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 标签业务：标签页展示标签及在架歌单数量（开发手册 5.2） */
public class TagService {

    private final TagDao tagDao = new TagDao();

    public List<TagDto> listAll() {
        return DbUtil.withConnection(conn -> {
            Map<Long, Long> counts = tagDao.countOnlinePlaylists(conn);
            List<TagDto> list = new ArrayList<>();
            for (Tag t : tagDao.findAll(conn)) {
                list.add(new TagDto(t.getId(), t.getName(), counts.getOrDefault(t.getId(), 0L)));
            }
            return list;
        });
    }
}