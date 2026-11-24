package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.PlaylistDao;
import com.chenlumina.musicweb.dto.PageDto;
import com.chenlumina.musicweb.dto.PlaylistCardDto;
import com.chenlumina.musicweb.dto.PlaylistDetailDto;
import com.chenlumina.musicweb.dto.TrackDto;
import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.entity.ShelfStatus;
import com.chenlumina.musicweb.entity.Tag;
import com.chenlumina.musicweb.entity.Track;
import com.chenlumina.musicweb.util.DbUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** 歌单业务：标签筛选、服务端分页、详情（前台仅上架可见） */
public class PlaylistService {

    private final PlaylistDao playlistDao = new PlaylistDao();

    public PageDto<PlaylistCardDto> search(ShelfStatus status, String tagName, int page, int size) {
        return DbUtil.withConnection(conn -> {
            List<Playlist> list = playlistDao.search(conn, status, tagName, page, size);
            long total = playlistDao.count(conn, status, tagName);
            List<Long> ids = list.stream().map(Playlist::getId).collect(Collectors.toList());
            Map<Long, List<Tag>> tagMap = playlistDao.tagsOf(conn, ids);
            List<PlaylistCardDto> cards = new ArrayList<>();
            for (Playlist p : list) {
                cards.add(toCard(p, tagMap.getOrDefault(p.getId(), Collections.emptyList())));
            }
            return new PageDto<>(cards, total, page, size);
        });
    }

    public Optional<PlaylistDetailDto> detail(Long id) {
        return DbUtil.withConnection(conn -> {
            Playlist p = playlistDao.findOnlineById(conn, id);
            if (p == null) {
                return Optional.empty();
            }
            List<TrackDto> tracks = new ArrayList<>();
            for (Track t : playlistDao.tracksOf(conn, id)) {
                tracks.add(new TrackDto(t.getTrackNo(), t.getTitle(), t.getDurationSec()));
            }
            List<String> tags = names(playlistDao.tagsOfOne(conn, id));
            return Optional.of(new PlaylistDetailDto(p.getId(), p.getTitle(), p.getDescription(),
                    p.getCoverUrl(), tags, p.getFavoriteCount(), tracks));
        });
    }

    private PlaylistCardDto toCard(Playlist p, List<Tag> tags) {
        return new PlaylistCardDto(p.getId(), p.getTitle(), p.getCoverUrl(),
                names(tags), p.getFavoriteCount());
    }

    private List<String> names(List<Tag> tags) {
        List<String> names = new ArrayList<>();
        for (Tag t : tags) {
            names.add(t.getName());
        }
        return names;
    }
}