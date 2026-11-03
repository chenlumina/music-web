package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.TagDao;
import com.chenlumina.musicweb.dto.TagDto;
import com.chenlumina.musicweb.util.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

/** 标签业务：标签页展示标签及歌单数量（开发手册 5.2） */
public class TagService {

    private final TagDao tagDao = new TagDao();

    public List<TagDto> listAll() {
        try (EntityManager em = JpaUtil.em()) {
            var counts = tagDao.countOnlinePlaylists(em);
            return tagDao.findAll(em).stream()
                    .map(t -> new TagDto(t.getId(), t.getName(), counts.getOrDefault(t.getId(), 0L)))
                    .toList();
        }
    }
}
