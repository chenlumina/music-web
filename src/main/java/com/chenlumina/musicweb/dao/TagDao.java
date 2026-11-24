package com.chenlumina.musicweb.dao;

import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.entity.ShelfStatus;
import com.chenlumina.musicweb.entity.Tag;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** 标签查询：全部标签、标签下歌单数、后台维护（开发手册 5.2/5.6） */
public class TagDao {

    public List<Tag> findAll(Connection conn) throws SQLException {
        String sql = "select id, name, sort_order from tags order by sort_order asc, id asc";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Tag> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        }
    }

    /** 标签 -> 在架歌单数；无歌单的标签计数为 0，也要出现在标签页 */
    public Map<Long, Long> countOnlinePlaylists(Connection conn) throws SQLException {
        String sql = "select pt.tag_id, count(p.id) from playlist_tag pt "
                + "join playlists p on p.id = pt.playlist_id "
                + "where p.status = 'ONLINE' group by pt.tag_id";
        return counts(conn, sql);
    }

    /** 各标签绑定歌单数（含下架，运营视角；无绑定的标签计数 0） */
    public Map<Long, Long> countBoundPlaylists(Connection conn) throws SQLException {
        String sql = "select pt.tag_id, count(*) from playlist_tag pt "
                + "join playlists p on p.id = pt.playlist_id group by pt.tag_id";
        return counts(conn, sql);
    }

    private Map<Long, Long> counts(Connection conn, String sql) throws SQLException {
        Map<Long, Long> result = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.put(rs.getLong(1), rs.getLong(2));
            }
        }
        return result;
    }

    public Optional<Tag> findByName(Connection conn, String name) throws SQLException {
        String sql = "select id, name, sort_order from tags where name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Tag findById(Connection conn, long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "select id, name, sort_order from tags where id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** 已存在的标签 id 集合，用于整体替换时校验非法标签 */
    public Set<Long> existingIds(Connection conn, Collection<Long> ids) throws SQLException {
        Set<Long> result = new HashSet<>();
        if (ids.isEmpty()) {
            return result;
        }
        StringBuilder sql = new StringBuilder("select id from tags where id in (");
        for (int i = 0; i < ids.size(); i++) {
            sql.append(i > 0 ? ",?" : "?");
        }
        sql.append(')');
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            for (Long id : ids) {
                ps.setLong(i++, id);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getLong(1));
                }
            }
        }
        return result;
    }

    /** 绑定该标签的歌单（只取运营判断所需字段） */
    public List<Playlist> playlistsBoundTo(Connection conn, long tagId) throws SQLException {
        String sql = "select p.id, p.status from playlists p "
                + "join playlist_tag pt on pt.playlist_id = p.id where pt.tag_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tagId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Playlist> list = new ArrayList<>();
                while (rs.next()) {
                    Playlist p = new Playlist();
                    p.setId(rs.getLong("id"));
                    p.setStatus(ShelfStatus.valueOf(rs.getString("status")));
                    list.add(p);
                }
                return list;
            }
        }
    }

    public long countTagsOfPlaylist(Connection conn, long playlistId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "select count(*) from playlist_tag where playlist_id = ?")) {
            ps.setLong(1, playlistId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    public long insert(Connection conn, Tag tag) throws SQLException {
        String sql = "insert into tags (name, sort_order, created_at, updated_at) values (?, ?, now(), now())";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tag.getName());
            ps.setInt(2, tag.getSortOrder());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    tag.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("插入标签后未取得自增主键");
    }

    public void update(Connection conn, Tag tag) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "update tags set name = ?, sort_order = ?, updated_at = now() where id = ?")) {
            ps.setString(1, tag.getName());
            ps.setInt(2, tag.getSortOrder());
            ps.setLong(3, tag.getId());
            ps.executeUpdate();
        }
    }

    /** 解除标签与全部歌单的关联（强制删除前调用） */
    public void unbindAll(Connection conn, long tagId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "delete from playlist_tag where tag_id = ?")) {
            ps.setLong(1, tagId);
            ps.executeUpdate();
        }
    }

    public void delete(Connection conn, long tagId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("delete from tags where id = ?")) {
            ps.setLong(1, tagId);
            ps.executeUpdate();
        }
    }

    private Tag map(ResultSet rs) throws SQLException {
        Tag t = new Tag();
        t.setId(rs.getLong("id"));
        t.setName(rs.getString("name"));
        t.setSortOrder(rs.getInt("sort_order"));
        return t;
    }
}