package com.chenlumina.musicweb.dao;

import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.util.DaoException;
import com.chenlumina.musicweb.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** 收藏持久层：收藏表与歌单收藏计数在同一事务内更新 */
public class FavoriteDao {

    public boolean exists(long userId, long playlistId) {
        String sql = "select count(*) from favorites where user_id = ? and playlist_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, playlistId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getLong(1) > 0;
            }
        } catch (SQLException e) {
            throw new DaoException("查询收藏状态失败", e);
        }
    }

    public void add(long userId, long playlistId) {
        try (Connection conn = DbUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "insert into favorites (user_id, playlist_id) values (?, ?)")) {
                    ps.setLong(1, userId);
                    ps.setLong(2, playlistId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "update playlists set favorite_count = favorite_count + 1 where id = ?")) {
                    ps.setLong(1, playlistId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DaoException("收藏失败", e);
        }
    }

    public void remove(long userId, long playlistId) {
        try (Connection conn = DbUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "delete from favorites where user_id = ? and playlist_id = ?")) {
                    ps.setLong(1, userId);
                    ps.setLong(2, playlistId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "update playlists set favorite_count = greatest(favorite_count - 1, 0) where id = ?")) {
                    ps.setLong(1, playlistId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DaoException("取消收藏失败", e);
        }
    }

    /** 我的收藏：按收藏时间倒序返回歌单 */
    public List<Playlist> listByUser(long userId) {
        String sql = "select p.id, p.title, p.description, p.cover_url, p.status, "
                + "p.favorite_count, p.created_at "
                + "from playlists p join favorites f on f.playlist_id = p.id "
                + "where f.user_id = ? order by f.created_at desc";
        List<Playlist> result = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("查询我的收藏失败", e);
        }
        return result;
    }

    private Playlist mapRow(ResultSet rs) throws SQLException {
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