package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.PlaylistDao;
import com.chenlumina.musicweb.dao.TagDao;
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
import com.chenlumina.musicweb.util.DbUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 后台歌单业务：增改、上下架（上架必须至少一个标签）、曲目整体替换（开发手册 5.6/6.2） */
public class AdminPlaylistService {

    private final PlaylistDao playlistDao = new PlaylistDao();
    private final TagDao tagDao = new TagDao();

    public PageDto<AdminPlaylistRowDto> search(String keyword, String status, int page, int size) {
        ShelfStatus st = parseStatus(status);
        return DbUtil.withConnection(conn -> {
            List<Playlist> list = playlistDao.searchAdmin(conn, keyword, st, page, size);
            long total = playlistDao.countAdmin(conn, keyword, st);
            List<Long> ids = list.stream().map(Playlist::getId).collect(Collectors.toList());
            Map<Long, Long> trackCounts = playlistDao.trackCounts(conn, ids);
            Map<Long, List<Tag>> tagMap = playlistDao.tagsOf(conn, ids);
            List<AdminPlaylistRowDto> rows = new ArrayList<>();
            for (Playlist p : list) {
                List<String> tags = names(tagMap.getOrDefault(p.getId(), Collections.emptyList()));
                rows.add(new AdminPlaylistRowDto(p.getId(), p.getTitle(), p.getCoverUrl(), p.getStatus(),
                        tags, trackCounts.getOrDefault(p.getId(), 0L).intValue(),
                        p.getFavoriteCount(), p.getUpdatedAt()));
            }
            return new PageDto<>(rows, total, page, size);
        });
    }

    public AdminPlaylistDetailDto detail(Long id) {
        return DbUtil.withConnection(conn -> {
            Playlist p = playlistDao.findById(conn, id);
            if (p == null) {
                throw new ApiException(404, "歌单不存在");
            }
            List<IdName> tags = new ArrayList<>();
            for (Tag t : playlistDao.tagsOfOne(conn, id)) {
                tags.add(new IdName(t.getId(), t.getName()));
            }
            List<AdminTrackDto> tracks = new ArrayList<>();
            for (Track t : playlistDao.tracksOf(conn, id)) {
                tracks.add(new AdminTrackDto(t.getId(), t.getTrackNo(), t.getTitle(), t.getDurationSec()));
            }
            return new AdminPlaylistDetailDto(p.getId(), p.getTitle(), p.getDescription(), p.getCoverUrl(),
                    p.getStatus(), p.getFavoriteCount(), tags, tracks);
        });
    }

    /** 新增歌统一律先下架，运营补全信息后再上架（上架需至少一个标签，开发手册 6.2） */
    public Long create(PlaylistUpsertReq req) {
        validate(req);
        return DbUtil.inTransaction(conn -> {
            Playlist p = new Playlist();
            applyFields(p, req);
            p.setStatus(ShelfStatus.OFFLINE);
            playlistDao.insert(conn, p);
            saveTags(conn, p.getId(), req.getTagIds());
            saveTracks(conn, p.getId(), req.getTracks());
            return p.getId();
        });
    }

    public void update(Long id, PlaylistUpsertReq req) {
        validate(req);
        DbUtil.inTransaction(conn -> {
            Playlist p = playlistDao.findById(conn, id);
            if (p == null) {
                throw new ApiException(404, "歌单不存在");
            }
            // 编辑不改变状态，但上架歌单必须始终保留至少一个标签（与 changeStatus 约束一致）
            if (p.getStatus() == ShelfStatus.ONLINE
                    && (req.getTagIds() == null || req.getTagIds().isEmpty())) {
                throw new ApiException(400, "上架歌单至少保留一个标签，请先下架再清空标签");
            }
            applyFields(p, req);
            playlistDao.update(conn, p);
            saveTags(conn, id, req.getTagIds());
            saveTracks(conn, id, req.getTracks());
            return null;
        });
    }

    /** 上下架独立入口：上架必须至少一个标签（开发手册 6.2） */
    public void changeStatus(Long id, String statusRaw) {
        ShelfStatus target = parseStatus(statusRaw);
        if (target == null) {
            throw new ApiException(400, "status 必须为 ONLINE 或 OFFLINE");
        }
        DbUtil.inTransaction(conn -> {
            Playlist p = playlistDao.findById(conn, id);
            if (p == null) {
                throw new ApiException(404, "歌单不存在");
            }
            if (target == ShelfStatus.ONLINE && tagDao.countTagsOfPlaylist(conn, id) == 0) {
                throw new ApiException(400, "上架前请先为歌单绑定至少一个标签");
            }
            playlistDao.updateStatus(conn, id, target);
            return null;
        });
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

    private List<String> names(List<Tag> tags) {
        List<String> names = new ArrayList<>();
        for (Tag t : tags) {
            names.add(t.getName());
        }
        return names;
    }

    private void validate(PlaylistUpsertReq req) {
        String title = req.getTitle() == null ? "" : req.getTitle().trim();
        if (title.isEmpty()) {
            throw new ApiException(400, "标题不能为空");
        }
        if (title.length() > 100) {
            throw new ApiException(400, "标题不能超过 100 字");
        }
        if (req.getDescription() != null && req.getDescription().length() > 2000) {
            throw new ApiException(400, "描述不能超过 2000 字");
        }
        if (req.getCoverUrl() != null && req.getCoverUrl().trim().length() > 255) {
            throw new ApiException(400, "封面地址过长");
        }
        List<TrackReq> tracks = req.getTracks() == null ? Collections.emptyList() : req.getTracks();
        if (tracks.size() > 100) {
            throw new ApiException(400, "曲目数量不能超过 100");
        }
        for (TrackReq t : tracks) {
            String name = t.getTitle() == null ? "" : t.getTitle().trim();
            if (name.isEmpty()) {
                throw new ApiException(400, "曲目名称不能为空");
            }
            if (name.length() > 100) {
                throw new ApiException(400, "曲目名称不能超过 100 字");
            }
            int d = t.getDurationSec() == null ? 0 : t.getDurationSec();
            if (d < 0 || d > 86400) {
                throw new ApiException(400, "曲目时长需在 0~86400 秒之间");
            }
        }
    }

    private void applyFields(Playlist p, PlaylistUpsertReq req) {
        p.setTitle(req.getTitle().trim());
        p.setDescription(req.getDescription() == null || req.getDescription().isBlank()
                ? null : req.getDescription().trim());
        p.setCoverUrl(req.getCoverUrl() == null || req.getCoverUrl().isBlank()
                ? null : req.getCoverUrl().trim());
    }

    /** 标签整体替换：去重保序，非法 id 直接 400，与服务端校验规则一致 */
    private void saveTags(Connection conn, long playlistId, List<Long> tagIds) throws SQLException {
        List<Long> distinct = new ArrayList<>(new LinkedHashSet<>(
                tagIds == null ? Collections.emptyList() : tagIds));
        if (!distinct.isEmpty()) {
            Set<Long> existing = tagDao.existingIds(conn, distinct);
            for (Long tagId : distinct) {
                if (!existing.contains(tagId)) {
                    throw new ApiException(400, "标签不存在：" + tagId);
                }
            }
        }
        playlistDao.replaceTags(conn, playlistId, distinct);
    }

    /** 曲目整体替换：按提交顺序重排曲目号，时长缺省 0 */
    private void saveTracks(Connection conn, long playlistId, List<TrackReq> tracks) throws SQLException {
        List<TrackReq> list = tracks == null ? Collections.emptyList() : tracks;
        List<Track> rows = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            TrackReq tr = list.get(i);
            Track t = new Track();
            t.setTrackNo(i + 1);
            t.setTitle(tr.getTitle().trim());
            t.setDurationSec(tr.getDurationSec() == null ? 0 : tr.getDurationSec());
            rows.add(t);
        }
        playlistDao.replaceTracks(conn, playlistId, rows);
    }
}