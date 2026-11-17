package com.chenlumina.musicweb.dao;

import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.util.DaoException;
import com.chenlumina.musicweb.util.DbUtil;

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
        String sql = "select id, title, description, cover_url, status, favorite_count, created_at "
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

    /** 分页查询：admin 列表用，按 id 倒序 */
    public List<Playlist> findPage(int offset, int limit) {
        String sql = "select id, title, description, cover_url, status, favorite_count, created_at "
                + "from playlists order by id desc limit ? offset ?";
        List<Playlist> result = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("分页查询歌单失败", e);
        }
        return result;
    }

    /** 歌单总数（分页用） */
    public long countAll() {
        String sql = "select count(*) from playlists";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new DaoException("统计歌单总数失败", e);
        }
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

    /** 按主键查单条，编辑回显用；查不到返回 null */
    public Playlist findById(long id) {
        String sql = "select id, title, description, cover_url, status, favorite_count, created_at "
                + "from playlists where id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DaoException("按主键查询歌单失败", e);
        }
    }

    /** 标题查重但排除自身：编辑时允许保留原标题 */
    public long countByTitleExcludeId(String title, long excludeId) {
        String sql = "select count(*) from playlists where title = ? and id <> ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setLong(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new DaoException("按标题统计歌单（排除自身）失败", e);
        }
    }

    /** 更新一条歌单，返回受影响行数 */
    public int update(Playlist playlist) {
        String sql = "update playlists set title = ?, description = ?, cover_url = ?, status = ? "
                + "where id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playlist.getTitle());
            ps.setString(2, playlist.getDescription());
            ps.setString(3, playlist.getCoverUrl());
            ps.setString(4, playlist.getStatus());
            ps.setLong(5, playlist.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("更新歌单失败", e);
        }
    }

    /** 前台只读列表：真实业务规则——仅 ONLINE 可见 */
    public List<Playlist> findOnline() {
        String sql = "select id, title, description, cover_url, status, favorite_count, created_at "
                + "from playlists where status = 'ONLINE' order by id desc";
        List<Playlist> result = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("查询上架歌单失败", e);
        }
        return result;
    }

    /** ResultSet -> 实体，集中在一处避免各个查询重复写映射 */
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
