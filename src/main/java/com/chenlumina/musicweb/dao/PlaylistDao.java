package com.chenlumina.musicweb.dao;

import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.entity.ShelfStatus;
import com.chenlumina.musicweb.entity.Tag;
import com.chenlumina.musicweb.entity.Track;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 歌单 JDBC 查询：分页、标签筛选、详情、后台维护（开发手册 4.2 Repository 层） */
public class PlaylistDao {

    private static final String COLS =
            "p.id, p.title, p.description, p.cover_url, p.status, p.favorite_count, p.created_at, p.updated_at";

    /** 标签筛选 + 上架状态过滤 + 服务端分页；tagName 为空查全部 */
    public List<Playlist> search(Connection conn, ShelfStatus status, String tagName, int page, int size)
            throws SQLException {
        boolean byTag = tagName != null && !tagName.isBlank();
        String sql = "select distinct " + COLS + " from playlists p "
                + "left join playlist_tag pt on pt.playlist_id = p.id "
                + "left join tags t on t.id = pt.tag_id "
                + "where p.status = ?" + (byTag ? " and t.name = ?" : "")
                + " order by p.id desc limit ? offset ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, status.name());
            if (byTag) {
                ps.setString(i++, tagName.trim());
            }
            ps.setInt(i++, size);
            ps.setInt(i, Math.max(0, (page - 1) * size));
            try (ResultSet rs = ps.executeQuery()) {
                List<Playlist> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }
        }
    }

    public long count(Connection conn, ShelfStatus status, String tagName) throws SQLException {
        boolean byTag = tagName != null && !tagName.isBlank();
        String sql = "select count(distinct p.id) from playlists p "
                + "left join playlist_tag pt on pt.playlist_id = p.id "
                + "left join tags t on t.id = pt.tag_id "
                + "where p.status = ?" + (byTag ? " and t.name = ?" : "");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            if (byTag) {
                ps.setString(2, tagName.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    /** 详情只对上架歌单开放；下架详情不可访问（开发手册 5.1） */
    public Playlist findOnlineById(Connection conn, long id) throws SQLException {
        return findOne(conn, "select " + COLS + " from playlists p where p.id = ? and p.status = 'ONLINE'", id);
    }

    /** 后台/收藏校验用：任意状态都能取到 */
    public Playlist findById(Connection conn, long id) throws SQLException {
        return findOne(conn, "select " + COLS + " from playlists p where p.id = ?", id);
    }

    /** 批量按 id 取歌单，我的收藏列表避免 N+1（开发手册 9.2） */
    public List<Playlist> findByIds(Connection conn, Collection<Long> ids) throws SQLException {
        List<Playlist> list = new ArrayList<>();
        if (ids.isEmpty()) {
            return list;
        }
        String sql = "select " + COLS + " from playlists p where p.id in ("
                + placeholders(ids.size()) + ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Long id : ids) {
                ps.setLong(i++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public boolean exists(Connection conn, long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("select 1 from playlists where id = ? limit 1")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Playlist findOne(Connection conn, String sql, long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** ===== 后台查询（开发手册 5.6）：关键字/状态筛选 + 分页 ===== */

    public List<Playlist> searchAdmin(Connection conn, String keyword, ShelfStatus status, int page, int size)
            throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "select " + COLS + " from playlists p" + adminWhere(keyword, status, params)
                + " order by p.id desc limit ? offset ?";
        params.add(size);
        params.add(Math.max(0, (page - 1) * size));
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<Playlist> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }
        }
    }

    public long countAdmin(Connection conn, String keyword, ShelfStatus status) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "select count(*) from playlists p" + adminWhere(keyword, status, params);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private String adminWhere(String keyword, ShelfStatus status, List<Object> params) {
        StringBuilder where = new StringBuilder(" where 1=1");
        if (status != null) {
            where.append(" and p.status = ?");
            params.add(status.name());
        }
        if (keyword != null && !keyword.isBlank()) {
            where.append(" and lower(p.title) like ?");
            params.add("%" + keyword.trim().toLowerCase() + "%");
        }
        return where.toString();
    }

    /** 批量统计曲目数，避免行内 N+1（开发手册 9.2） */
    public Map<Long, Long> trackCounts(Connection conn, Collection<Long> playlistIds) throws SQLException {
        Map<Long, Long> result = new HashMap<>();
        if (playlistIds.isEmpty()) {
            return result;
        }
        String sql = "select playlist_id, count(*) from tracks where playlist_id in ("
                + placeholders(playlistIds.size()) + ") group by playlist_id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Long id : playlistIds) {
                ps.setLong(i++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getLong(1), rs.getLong(2));
                }
            }
        }
        return result;
    }

    /** 批量取标签，按标签排序值排序；单次查询避免每个歌单各查一次 */
    public Map<Long, List<Tag>> tagsOf(Connection conn, Collection<Long> playlistIds) throws SQLException {
        Map<Long, List<Tag>> result = new LinkedHashMap<>();
        if (playlistIds.isEmpty()) {
            return result;
        }
        String sql = "select pt.playlist_id, t.id, t.name, t.sort_order "
                + "from playlist_tag pt join tags t on t.id = pt.tag_id "
                + "where pt.playlist_id in (" + placeholders(playlistIds.size()) + ") "
                + "order by pt.playlist_id asc, t.sort_order asc, t.id asc";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Long id : playlistIds) {
                ps.setLong(i++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long playlistId = rs.getLong(1);
                    Tag tag = new Tag();
                    tag.setId(rs.getLong(2));
                    tag.setName(rs.getString(3));
                    tag.setSortOrder(rs.getInt(4));
                    result.computeIfAbsent(playlistId, k -> new ArrayList<>()).add(tag);
                }
            }
        }
        return result;
    }

    public List<Tag> tagsOfOne(Connection conn, long playlistId) throws SQLException {
        return tagsOf(conn, Collections.singletonList(playlistId))
                .getOrDefault(playlistId, Collections.emptyList());
    }

    /** 曲目按曲目号排序，同号按主键兜底，保证展示稳定 */
    public List<Track> tracksOf(Connection conn, long playlistId) throws SQLException {
        String sql = "select id, track_no, title, duration_sec from tracks "
                + "where playlist_id = ? order by track_no asc, id asc";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, playlistId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Track> list = new ArrayList<>();
                while (rs.next()) {
                    Track t = new Track();
                    t.setId(rs.getLong("id"));
                    t.setTrackNo(rs.getInt("track_no"));
                    t.setTitle(rs.getString("title"));
                    t.setDurationSec(rs.getInt("duration_sec"));
                    list.add(t);
                }
                return list;
            }
        }
    }

    /** 批量取歌单标题，后台推荐位列表展示跳转目标用 */
    public Map<Long, String> findTitles(Connection conn, Collection<Long> ids) throws SQLException {
        Map<Long, String> result = new HashMap<>();
        if (ids.isEmpty()) {
            return result;
        }
        String sql = "select id, title from playlists where id in (" + placeholders(ids.size()) + ")";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Long id : ids) {
                ps.setLong(i++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getLong(1), rs.getString(2));
                }
            }
        }
        return result;
    }

    /** 新增歌单：返回自增主键；状态由服务层强制为下架 */
    public long insert(Connection conn, Playlist p) throws SQLException {
        String sql = "insert into playlists (title, description, cover_url, status, favorite_count, created_at, updated_at) "
                + "values (?, ?, ?, ?, ?, now(), now())";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getTitle());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getCoverUrl());
            ps.setString(4, p.getStatus().name());
            ps.setInt(5, p.getFavoriteCount());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    p.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("插入歌单后未取得自增主键");
    }

    /** 编辑保存只改内容字段；状态走独立入口，避免误改上下架 */
    public void update(Connection conn, Playlist p) throws SQLException {
        String sql = "update playlists set title = ?, description = ?, cover_url = ?, updated_at = now() where id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getTitle());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getCoverUrl());
            ps.setLong(4, p.getId());
            ps.executeUpdate();
        }
    }

    public void updateStatus(Connection conn, long id, ShelfStatus status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "update playlists set status = ?, updated_at = now() where id = ?")) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    /** 收藏数冗余计数增减（事务内调用，开发手册 5.3/9.2） */
    public void adjustFavoriteCount(Connection conn, long playlistId, int delta) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "update playlists set favorite_count = favorite_count + ? where id = ?")) {
            ps.setInt(1, delta);
            ps.setLong(2, playlistId);
            ps.executeUpdate();
        }
    }

    /** 标签整体替换：先清空再写入（事务内调用） */
    public void replaceTags(Connection conn, long playlistId, Collection<Long> tagIds) throws SQLException {
        try (PreparedStatement del = conn.prepareStatement("delete from playlist_tag where playlist_id = ?")) {
            del.setLong(1, playlistId);
            del.executeUpdate();
        }
        if (tagIds.isEmpty()) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "insert into playlist_tag (playlist_id, tag_id) values (?, ?)")) {
            for (Long tagId : tagIds) {
                ps.setLong(1, playlistId);
                ps.setLong(2, tagId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** 曲目整体替换：先清空旧行再按提交顺序重新编号（事务内调用） */
    public void replaceTracks(Connection conn, long playlistId, List<Track> tracks) throws SQLException {
        try (PreparedStatement del = conn.prepareStatement("delete from tracks where playlist_id = ?")) {
            del.setLong(1, playlistId);
            del.executeUpdate();
        }
        if (tracks.isEmpty()) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "insert into tracks (playlist_id, track_no, title, duration_sec, created_at) values (?, ?, ?, ?, now())")) {
            for (Track t : tracks) {
                ps.setLong(1, playlistId);
                ps.setInt(2, t.getTrackNo());
                ps.setString(3, t.getTitle());
                ps.setInt(4, t.getDurationSec());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    private String placeholders(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('?');
        }
        return sb.toString();
    }

    private Playlist map(ResultSet rs) throws SQLException {
        Playlist p = new Playlist();
        p.setId(rs.getLong("id"));
        p.setTitle(rs.getString("title"));
        p.setDescription(rs.getString("description"));
        p.setCoverUrl(rs.getString("cover_url"));
        p.setStatus(ShelfStatus.valueOf(rs.getString("status")));
        p.setFavoriteCount(rs.getInt("favorite_count"));
        p.setCreatedAt(toTime(rs, "created_at"));
        p.setUpdatedAt(toTime(rs, "updated_at"));
        return p;
    }

    /** DATETIME -> LocalDateTime 直接按字面值读取，避免时区二次换算 */
    public static LocalDateTime toTime(ResultSet rs, String column) throws SQLException {
        return rs.getObject(column, LocalDateTime.class);
    }
}