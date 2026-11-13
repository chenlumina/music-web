package com.chenlumina.musiclab.dao;

import com.chenlumina.musiclab.entity.User;
import com.chenlumina.musiclab.util.DaoException;
import com.chenlumina.musiclab.util.DbUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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