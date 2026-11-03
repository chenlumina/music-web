package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.PlaylistDao;
import com.chenlumina.musicweb.dto.PageDto;
import com.chenlumina.musicweb.dto.PlaylistCardDto;
import com.chenlumina.musicweb.dto.PlaylistDetailDto;
import com.chenlumina.musicweb.dto.TrackDto;
import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.entity.ShelfStatus;
import com.chenlumina.musicweb.entity.Tag;
import com.chenlumina.musicweb.util.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** 歌单业务：标签筛选、服务端分页、详情（前台仅上架可见） */
public class PlaylistService {

    private final PlaylistDao playlistDao = new PlaylistDao();

    public PageDto<PlaylistCardDto> search(ShelfStatus status, String tagName, int page, int size) {
        try (EntityManager em = JpaUtil.em()) {
            List<Playlist> list = playlistDao.search(em, status, tagName, page, size);
            long total = playlistDao.count(em, status, tagName);
            List<PlaylistCardDto> cards = list.stream()
                    .map(this::toCard)
                    .toList();
            return new PageDto<>(cards, total, page, size);
        }
    }

    public Optional<PlaylistDetailDto> detail(Long id) {
        try (EntityManager em = JpaUtil.em()) {
            return playlistDao.findOnlineById(em, id).map(p -> {
                List<TrackDto> tracks = p.getTracks().stream()
                        .map(t -> new TrackDto(t.getTrackNo(), t.getTitle(), t.getDurationSec()))
                        .toList();
                List<String> tags = p.getTags().stream()
                        .sorted(Comparator.comparing(Tag::getSortOrder))
                        .map(Tag::getName)
                        .toList();
                return new PlaylistDetailDto(p.getId(), p.getTitle(), p.getDescription(),
                        p.getCoverUrl(), tags, p.getFavoriteCount(), tracks);
            });
        }
    }

    /** 在 EntityManager 开启期内完成 lazy 集合到 DTO 的转换 */
    private PlaylistCardDto toCard(Playlist p) {
        List<String> tags = p.getTags().stream()
                .sorted(Comparator.comparing(Tag::getSortOrder))
                .map(Tag::getName)
                .toList();
        return new PlaylistCardDto(p.getId(), p.getTitle(), p.getCoverUrl(), tags, p.getFavoriteCount());
    }
}
