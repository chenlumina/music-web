package com.chenlumina.musicweb.dao;

import com.chenlumina.musicweb.util.DaoException;
import com.chenlumina.musicweb.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** 后台统计：只对白名单表做 count，避免拼接注入 */
public class AdminDao {

    private static final Set<String> TABLES =
            Set.of("playlists", "users", "favorites", "recommendations", "tracks", "tags");

    public Map<String, Long> stats() {
        Map<String, Long> map = new LinkedHashMap<>();
        for (String table : new String[]{"playlists", "users", "favorites", "recommendations", "tracks", "tags"}) {
            map.put(table, count(table));
        }
        return map;
    }

    private long count(String table) {
        if (!TABLES.contains(table)) {
            throw new IllegalArgumentException("非法表名");
        }
        String sql = "select count(*) from " + table;
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new DaoException("统计 " + table + " 失败", e);
        }
    }
}