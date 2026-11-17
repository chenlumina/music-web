package com.chenlumina.musicweb.dao;

import com.chenlumina.musicweb.entity.User;
import com.chenlumina.musicweb.util.DaoException;
import com.chenlumina.musicweb.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 用户持久层：与 PlaylistDao 共用 DbUtil 取连接，
 * SQLException 统一包装成 DaoException，由容器 error-page 兜底。
 */
public class UserDao {

    /** 按用户名查用户，查不到返回 null；登录失败不区分“账号不存在”与“密码错误” */
    public User findByUsername(String username) {
        String sql = "select id, username, password_hash, role, created_at from users where username = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            throw new DaoException("按用户名查询用户失败", e);
        }
    }

    /** 用户名是否已存在（注册查重用） */
    public boolean existsByUsername(String username) {
        String sql = "select count(*) from users where username = ?";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getLong(1) > 0;
            }
        } catch (SQLException e) {
            throw new DaoException("按用户名统计用户失败", e);
        }
    }

    /** 新增用户，返回数据库生成的主键 */
    public long insert(User user) {
        String sql = "insert into users (username, password_hash, role) values (?, ?, ?)";
        try (Connection conn = DbUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new DaoException("新增用户失败", e);
        }
    }

    /** ResultSet -> 实体，与 PlaylistDao 相同的集中映射写法 */
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return user;
    }
}