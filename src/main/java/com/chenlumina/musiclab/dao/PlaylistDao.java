package com.chenlumina.musiclab.dao;

import com.chenlumina.musiclab.entity.Playlist;
import com.chenlumina.musiclab.util.DaoException;
import com.chenlumina.musiclab.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 歌单持久层：只用 JDBC PreparedStatement 完成查询与插入。
 * Connection / PreparedStatement / ResultSet 全部用 try-with-resources 保证关闭。
 */
public class PlaylistDao {

    /** 列表查询：按 id 倒序，最新的歌单排在最前 */
    public List<Playlist> findAll() {
        String sql = "select id, title, description, cover_url, status, created_at "
                + "from playlists order by id desc";
        List<Playlist> result = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("查询歌单列表失败", e);
        }
        return result;
    }

    /** 标题查重：Service 的业务校验要用，返回同名记录数 */
    public long countByTitle(String title) {
        String sql = "select count(*) from playlists where title = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new DaoException("按标题统计歌单失败", e);
        }
    }

    /** 新增一条歌单，返回数据库生成的主键 */
    public long insert(Playlist playlist) {
        String sql = "insert into playlists (title, description, cover_url, status) values (?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, playlist.getTitle());
            ps.setString(2, playlist.getDescription());
            ps.setString(3, playlist.getCoverUrl());
            ps.setString(4, playlist.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new DaoException("新增歌单失败", e);
        }
    }

    /** ResultSet -> 实体，集中在一处避免各个查询重复写映射 */
    private Playlist mapRow(ResultSet rs) throws SQLException {
        Playlist playlist = new Playlist();
        playlist.setId(rs.getLong("id"));
        playlist.setTitle(rs.getString("title"));
        playlist.setDescription(rs.getString("description"));
        playlist.setCoverUrl(rs.getString("cover_url"));
        playlist.setStatus(rs.getString("status"));
        playlist.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return playlist;
    }
}
