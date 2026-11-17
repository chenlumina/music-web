package com.chenlumina.musicweb.dao;

import com.chenlumina.musicweb.entity.Track;
import com.chenlumina.musicweb.util.DaoException;
import com.chenlumina.musicweb.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** 曲目持久层 */
public class TrackDao {

    public List<Track> findByPlaylist(long playlistId) {
        String sql = "select id, playlist_id, title, artist, duration_sec, sort_order "
                + "from tracks where playlist_id = ? order by sort_order, id";
        List<Track> result = new ArrayList<>();
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, playlistId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("查询曲目失败", e);
        }
        return result;
    }

    public long insert(Track track) {
        String sql = "insert into tracks (playlist_id, title, artist, duration_sec) values (?, ?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, track.getPlaylistId());
            ps.setString(2, track.getTitle());
            ps.setString(3, track.getArtist());
            ps.setInt(4, track.getDurationSec());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new DaoException("新增曲目失败", e);
        }
    }

    public int delete(long id) {
        String sql = "delete from tracks where id = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("删除曲目失败", e);
        }
    }

    private Track mapRow(ResultSet rs) throws SQLException {
        Track track = new Track();
        track.setId(rs.getLong("id"));
        track.setPlaylistId(rs.getLong("playlist_id"));
        track.setTitle(rs.getString("title"));
        track.setArtist(rs.getString("artist"));
        track.setDurationSec(rs.getInt("duration_sec"));
        track.setSortOrder(rs.getInt("sort_order"));
        return track;
    }
}