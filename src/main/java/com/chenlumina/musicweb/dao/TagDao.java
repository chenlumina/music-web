package com.chenlumina.musicweb.dao;

import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.entity.Tag;
import com.chenlumina.musicweb.util.DaoException;
import com.chenlumina.musicweb.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** 标签持久层：标签字典、歌单标签关联、按标签筛选上架歌单 */
public class TagDao {

    public List<Tag> findAll() {
        String sql = "select id, name, sort_order from tags order by sort_order, id";
        List<Tag> result = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapTag(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("查询标签失败", e);
        }
        return result;
    }

    public List<Tag> findByPlaylist(long playlistId) {
        String sql = "select t.id, t.name, t.sort_order from tags t "
                + "join playlist_tag pt on pt.tag_id = t.id "
                + "where pt.playlist_id = ? order by t.sort_order, t.id";
        List<Tag> result = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, playlistId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapTag(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("查询歌单标签失败", e);
        }
        return result;
    }

    public List<Long> findIdsByPlaylist(long playlistId) {
        String sql = "select tag_id from playlist_tag where playlist_id = ?";
        List<Long> ids = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, playlistId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("查询歌单标签ID失败", e);
        }
        return ids;
    }

    /** 全量重置某歌单的标签 */
    public void assign(long playlistId, List<Long> tagIds) {
        try (Connection conn = DbUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement del = conn.prepareStatement(
                        "delete from playlist_tag where playlist_id = ?")) {
                    del.setLong(1, playlistId);
                    del.executeUpdate();
                }
                try (PreparedStatement ins = conn.prepareStatement(
                        "insert into playlist_tag (playlist_id, tag_id) values (?, ?)")) {
                    for (Long tagId : tagIds) {
                        ins.setLong(1, playlistId);
                        ins.setLong(2, tagId);
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DaoException("保存歌单标签失败", e);
        }
    }

    /** 按标签筛选上架歌单 */
    public List<Playlist> findOnlineByTag(long tagId) {
        String sql = "select p.id, p.title, p.description, p.cover_url, p.status, "
                + "p.favorite_count, p.created_at from playlists p "
                + "join playlist_tag pt on pt.playlist_id = p.id "
                + "where pt.tag_id = ? and p.status = 'ONLINE' order by p.id desc";
        List<Playlist> result = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, tagId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapPlaylist(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("按标签查询歌单失败", e);
        }
        return result;
    }

    private Tag mapTag(ResultSet rs) throws SQLException {
        Tag tag = new Tag();
        tag.setId(rs.getLong("id"));
        tag.setName(rs.getString("name"));
        tag.setSortOrder(rs.getInt("sort_order"));
        return tag;
    }

    private Playlist mapPlaylist(ResultSet rs) throws SQLException {
        Playlist playlist = new Playlist();
        playlist.setId(rs.getLong("id"));
        playlist.setTitle(rs.getString("title"));
        playlist.setDescription(rs.getString("description"));
        playlist.setCoverUrl(rs.getString("cover_url"));
        playlist.setStatus(rs.getString("status"));
        playlist.setFavoriteCount(rs.getInt("favorite_count"));
        playlist.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return playlist;
    }
}