package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.PlaylistDao;
import com.chenlumina.musicweb.dto.AdminPlaylistDetailDto;
import com.chenlumina.musicweb.dto.AdminPlaylistRowDto;
import com.chenlumina.musicweb.dto.AdminTrackDto;
import com.chenlumina.musicweb.dto.IdName;
import com.chenlumina.musicweb.dto.PageDto;
import com.chenlumina.musicweb.dto.PlaylistUpsertReq;
import com.chenlumina.musicweb.dto.TrackReq;
import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.entity.ShelfStatus;
import com.chenlumina.musicweb.entity.Tag;
import com.chenlumina.musicweb.entity.Track;
import com.chenlumina.musicweb.util.ApiException;
import com.chenlumina.musicweb.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.Set;

/** 后台歌单业务：增改、上下架（上架必须至少一个标签）、曲目整体替换（开发手册 5.6/6.2） */
public class AdminPlaylistService {

    private final PlaylistDao playlistDao = new PlaylistDao();

    public PageDto<AdminPlaylistRowDto> search(String keyword, String status, int page, int size) {
        ShelfStatus st = parseStatus(status);
        try (EntityManager em = JpaUtil.em()) {
            List<Playlist> list = playlistDao.searchAdmin(em, keyword, st, page, size);
            long total = playlistDao.countAdmin(em, keyword, st);
            List<Long> ids = list.stream().map(Playlist::getId).toList();
            Map<Long, Long> trackCounts = playlistDao.trackCounts(em, ids);
            List<AdminPlaylistRowDto> rows = list.stream()
                    .map(p -> new AdminPlaylistRowDto(p.getId(), p.getTitle(), p.getCoverUrl(), p.getStatus(),
                            tagNames(p), trackCounts.getOrDefault(p.getId(), 0L).intValue(),
                            p.getFavoriteCount(), p.getUpdatedAt()))
                    .toList();
            return new PageDto<>(rows, total, page, size);
        }
    }

    public AdminPlaylistDetailDto detail(Long id) {
        try (EntityManager em = JpaUtil.em()) {
            Playlist p = em.find(Playlist.class, id);
            if (p == null) {
                throw new ApiException(404, "歌单不存在");
            }
            List<AdminTrackDto> tracks = p.getTracks().stream()
                    .map(t -> new AdminTrackDto(t.getId(), t.getTrackNo(), t.getTitle(), t.getDurationSec()))
                    .toList();
            List<IdName> tags = p.getTags().stream()
                    .sorted(Comparator.comparing(Tag::getSortOrder))
                    .map(t -> new IdName(t.getId(), t.getName()))
                    .toList();
            return new AdminPlaylistDetailDto(p.getId(), p.getTitle(), p.getDescription(), p.getCoverUrl(),
                    p.getStatus(), p.getFavoriteCount(), tags, tracks);
        }
    }

    /** 新增歌统一律先下架，运营补全信息后再上架（上架需至少一个标签，开发手册 6.2） */
    public Long create(PlaylistUpsertReq req) {
        validate(req);
        try (EntityManager em = JpaUtil.em()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Playlist p = new Playlist();
                applyFields(p, req);
                p.setStatus(ShelfStatus.OFFLINE);
                em.persist(p);
                bindTags(em, p, req.tagIds());
                replaceTracks(em, p, req.tracks());
                tx.commit();
                return p.getId();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
        }
    }

    public void update(Long id, PlaylistUpsertReq req) {
        validate(req);
        try (EntityManager em = JpaUtil.em()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Playlist p = em.find(Playlist.class, id);
                if (p == null) {
                    throw new ApiException(404, "歌单不存在");
                }
                // 编辑不改变状态，但上架歌单必须始终保留至少一个标签（与 changeStatus 约束一致）
                if (p.getStatus() == ShelfStatus.ONLINE
                        && (req.tagIds() == null || req.tagIds().isEmpty())) {
                    throw new ApiException(400, "上架歌单至少保留一个标签，请先下架再清空标签");
                }
                applyFields(p, req);
                p.getTags().clear();
                bindTags(em, p, req.tagIds());
                replaceTracks(em, p, req.tracks());
                tx.commit();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
        }
    }

    /** 上下架独立入口：上架必须至少一个标签（开发手册 6.2） */
    public void changeStatus(Long id, String statusRaw) {
        ShelfStatus target = parseStatus(statusRaw);
        if (target == null) {
            throw new ApiException(400, "status 必须为 ONLINE 或 OFFLINE");
        }
        try (EntityManager em = JpaUtil.em()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Playlist p = em.find(Playlist.class, id);
                if (p == null) {
                    throw new ApiException(404, "歌单不存在");
                }
                if (target == ShelfStatus.ONLINE && p.getTags().isEmpty()) {
                    throw new ApiException(400, "上架前请先为歌单绑定至少一个标签");
                }
                p.setStatus(target);
                tx.commit();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
        }
    }

    // ----- 内部 -----

    /** search 用：空值表示不筛；changeStatus 复用，空值由调用方判 400 */
    private ShelfStatus parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return ShelfStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(400, "status 必须为 ONLINE 或 OFFLINE");
        }
    }

    private List<String> tagNames(Playlist p) {
        return p.getTags().stream()
                .sorted(Comparator.comparing(Tag::getSortOrder))
                .map(Tag::getName)
                .toList();
    }

    private void validate(PlaylistUpsertReq req) {
        String title = req.title() == null ? "" : req.title().trim();
        if (title.isEmpty()) {
            throw new ApiException(400, "标题不能为空");
        }
        if (title.length() > 100) {
            throw new ApiException(400, "标题不能超过 100 字");
        }
        if (req.description() != null && req.description().length() > 2000) {
            throw new ApiException(400, "描述不能超过 2000 字");
        }
        if (req.coverUrl() != null && req.coverUrl().trim().length() > 255) {
            throw new ApiException(400, "封面地址过长");
        }
        List<TrackReq> tracks = req.tracks() == null ? List.of() : req.tracks();
        if (tracks.size() > 100) {
            throw new ApiException(400, "曲目数量不能超过 100");
        }
        for (TrackReq t : tracks) {
            String name = t.title() == null ? "" : t.title().trim();
            if (name.isEmpty()) {
                throw new ApiException(400, "曲目名称不能为空");
            }
            if (name.length() > 100) {
                throw new ApiException(400, "曲目名称不能超过 100 字");
            }
            int d = t.durationSec() == null ? 0 : t.durationSec();
            if (d < 0 || d > 86400) {
                throw new ApiException(400, "曲目时长需在 0~86400 秒之间");
            }
        }
    }

    private void applyFields(Playlist p, PlaylistUpsertReq req) {
        p.setTitle(req.title().trim());
        p.setDescription(req.description() == null || req.description().isBlank() ? null : req.description().trim());
        p.setCoverUrl(req.coverUrl() == null || req.coverUrl().isBlank() ? null : req.coverUrl().trim());
    }

    private void bindTags(EntityManager em, Playlist p, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        Set<Long> distinct = new LinkedHashSet<>(tagIds);
        for (Long tagId : distinct) {
            Tag tag = em.find(Tag.class, tagId);
            if (tag == null) {
                throw new ApiException(400, "标签不存在：" + tagId);
            }
            p.getTags().add(tag);
        }
    }

    private void replaceTracks(EntityManager em, Playlist p, List<TrackReq> tracks) {
        playlistDao.deleteTracksOf(em, p.getId());
        p.getTracks().clear();
        List<TrackReq> list = tracks == null ? List.of() : tracks;
        for (int i = 0; i < list.size(); i++) {
            TrackReq t = list.get(i);
            Track track = new Track();
            track.setPlaylist(p);
            track.setTrackNo(i + 1);
            track.setTitle(t.title().trim());
            track.setDurationSec(t.durationSec() == null ? 0 : t.durationSec());
            em.persist(track);
        }
    }
}
