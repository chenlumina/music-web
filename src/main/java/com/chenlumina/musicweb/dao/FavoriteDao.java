package com.chenlumina.musicweb.dao;

import com.chenlumina.musicweb.entity.Favorite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** 收藏查询与写入；user_id + playlist_id 唯一约束由数据库兜底（开发手册 5.3） */
public class FavoriteDao {

    public boolean exists(Connection conn, long userId, long playlistId) throws SQLException {
        String sql = "select 1 from favorites where user_id = ? and playlist_id = ? limit 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, playlistId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insert(Connection conn, long userId, long playlistId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "insert into favorites (user_id, playlist_id, created_at) values (?, ?, now())")) {
            ps.setLong(1, userId);
            ps.setLong(2, playlistId);
            ps.executeUpdate();
        }
    }

    /** @return 删除行数（0 = 本来就没收藏） */
    public int delete(Connection conn, long userId, long playlistId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "delete from favorites where user_id = ? and playlist_id = ?")) {
            ps.setLong(1, userId);
            ps.setLong(2, playlistId);
            return ps.executeUpdate();
        }
    }

    /** 我的收藏按收藏时间倒序、分页（开发手册 5.3） */
    public List<Favorite> pageByUser(Connection conn, long userId, int page, int size) throws SQLException {
        String sql = "select id, user_id, playlist_id, created_at from favorites "
                + "where user_id = ? order by created_at desc, id desc limit ? offset ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, size);
            ps.setInt(3, Math.max(0, (page - 1) * size));
            try (ResultSet rs = ps.executeQuery()) {
                List<Favorite> list = new ArrayList<>();
                while (rs.next()) {
                    Favorite f = new Favorite();
                    f.setId(rs.getLong("id"));
                    f.setUserId(rs.getLong("user_id"));
                    f.setPlaylistId(rs.getLong("playlist_id"));
                    f.setCreatedAt(PlaylistDao.toTime(rs, "created_at"));
                    list.add(f);
                }
                return list;
            }
        }
    }

    public long countByUser(Connection conn, long userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "select count(*) from favorites where user_id = ?")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
}