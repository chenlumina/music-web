package com.chenlumina.musicweb.dao;

import com.chenlumina.musicweb.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/** 用户查询与写入（开发手册 4.2 Repository 层）；username 唯一由数据库约束兜底 */
public class UserDao {

    public Optional<User> findByUsername(Connection conn, String username) throws SQLException {
        String sql = "select id, username, password_hash, role from users where username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public boolean existsByUsername(Connection conn, String username) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("select 1 from users where username = ? limit 1")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** 插入用户并回填自增 id */
    public long insert(Connection conn, User user) throws SQLException {
        String sql = "insert into users (username, password_hash, role, created_at, updated_at) "
                + "values (?, ?, ?, now(), now())";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    user.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("插入用户后未取得自增主键");
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        return u;
    }
}