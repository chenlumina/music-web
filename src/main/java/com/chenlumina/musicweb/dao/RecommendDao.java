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

/** 推荐持久层：推荐标记与上架推荐歌单查询 */
public class RecommendDao {

    public boolean exists(long playlistId) {
        String sql = "select count(*) from recommendations where playlist_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, playlistId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getLong(1) > 0;
            }
        } catch (SQLException e) {
            throw new DaoException("查询推荐状态失败", e);
        }
    }

    public void add(long playlistId) {
        String sql = "insert into recommendations (playlist_id) values (?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, playlistId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("加入推荐失败", e);
        }
    }

    public void remove(long playlistId) {
        String sql = "delete from recommendations where playlist_id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, playlistId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("取消推荐失败", e);
        }
    }

    /** 上架且被推荐的歌单，按推荐排序 */
    public List<Playlist> findOnlineRecommended() {
        String sql = "select p.id, p.title, p.description, p.cover_url, p.status, "
                + "p.favorite_count, p.created_at from playlists p "
                + "join recommendations r on r.playlist_id = p.id "
                + "where p.status = 'ONLINE' order by r.sort_order, r.id";
        List<Playlist> result = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("查询推荐歌单失败", e);
        }
        return result;
    }

    public List<Long> findIds() {
        String sql = "select playlist_id from recommendations";
        List<Long> ids = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getLong(1));
            }
        } catch (SQLException e) {
            throw new DaoException("查询推荐ID失败", e);
        }
        return ids;
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