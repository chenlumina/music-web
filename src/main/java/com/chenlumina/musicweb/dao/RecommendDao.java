package com.chenlumina.musicweb.dao;

import com.chenlumina.musicweb.entity.Recommendation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** 推荐位读写：前台只读启用项，后台维护全部配置（开发手册 5.4，纯配置无算法） */
public class RecommendDao {

    public List<Recommendation> findEnabled(Connection conn) throws SQLException {
        return query(conn, "select " + COLS + " from recommendations where enabled = 1 "
                + "order by sort_order asc, id asc");
    }

    /** 后台：全部推荐位（含停用），按排序（开发手册 5.6） */
    public List<Recommendation> findAll(Connection conn) throws SQLException {
        return query(conn, "select " + COLS + " from recommendations order by sort_order asc, id asc");
    }

    public Recommendation findById(Connection conn, long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "select " + COLS + " from recommendations where id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private static final String COLS =
            "id, title, image_url, playlist_id, sort_order, enabled";

    private List<Recommendation> query(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Recommendation> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        }
    }

    public long insert(Connection conn, Recommendation r) throws SQLException {
        String sql = "insert into recommendations "
                + "(title, image_url, playlist_id, sort_order, enabled, created_at, updated_at) "
                + "values (?, ?, ?, ?, ?, now(), now())";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getTitle());
            ps.setString(2, r.getImageUrl());
            setNullableLong(ps, 3, r.getPlaylistId());
            ps.setInt(4, r.getSortOrder());
            ps.setBoolean(5, r.isEnabled());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    r.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("插入推荐位后未取得自增主键");
    }

    public void update(Connection conn, Recommendation r) throws SQLException {
        String sql = "update recommendations set title = ?, image_url = ?, playlist_id = ?, "
                + "sort_order = ?, enabled = ?, updated_at = now() where id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getTitle());
            ps.setString(2, r.getImageUrl());
            setNullableLong(ps, 3, r.getPlaylistId());
            ps.setInt(4, r.getSortOrder());
            ps.setBoolean(5, r.isEnabled());
            ps.setLong(6, r.getId());
            ps.executeUpdate();
        }
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.BIGINT);
        } else {
            ps.setLong(index, value);
        }
    }

    private Recommendation map(ResultSet rs) throws SQLException {
        Recommendation r = new Recommendation();
        r.setId(rs.getLong("id"));
        r.setTitle(rs.getString("title"));
        r.setImageUrl(rs.getString("image_url"));
        long playlistId = rs.getLong("playlist_id");
        r.setPlaylistId(rs.wasNull() ? null : playlistId);
        r.setSortOrder(rs.getInt("sort_order"));
        r.setEnabled(rs.getBoolean("enabled"));
        return r;
    }
}