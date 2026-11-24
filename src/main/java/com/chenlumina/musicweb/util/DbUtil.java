package com.chenlumina.musicweb.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * JDBC 连接与事务边界工具：连接信息外置在 classpath 的 db.properties。
 * 所有 DAO 统一从这里拿 Connection，服务和 DAO 都不直接管理 URL/账号。
 */
public final class DbUtil {

    /** 事务/查询回调：受检 SQLException 在工具内统一包装 */
    @FunctionalInterface
    public interface Work<T> {
        T run(Connection conn) throws SQLException;
    }

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in = DbUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("classpath 下找不到 db.properties");
            }
            props.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("读取 db.properties 失败：" + e.getMessage());
        }
        URL = envOr("DB_URL", props.getProperty("db.url"));
        USER = envOr("DB_USERNAME", props.getProperty("db.username"));
        PASSWORD = envOr("DB_PASSWORD", props.getProperty("db.password"));
        try {
            // 显式加载驱动，驱动缺失时在启动阶段直接暴露问题
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL 驱动未找到，请检查 mysql-connector-j 是否在 WEB-INF/lib 中");
        }
    }

    /** 只读查询：连接用完即关 */
    public static <T> T withConnection(Work<T> work) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            return work.run(conn);
        } catch (SQLException e) {
            throw new DaoException("数据库操作失败", e);
        }
    }

    /** 写操作事务：正常提交，异常回滚；ApiException 原样透传给上层转业务错误码 */
    public static <T> T inTransaction(Work<T> work) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);
            try {
                T result = work.run(conn);
                conn.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                    // 回滚失败不覆盖原始异常
                }
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new DaoException("数据库事务执行失败", e);
            }
        } catch (SQLException e) {
            throw new DaoException("数据库连接失败", e);
        }
    }

    /** 唯一键冲突判定：MySQL SQLState 23000（收藏重复、标签重名由数据库约束兜底） */
    public static boolean isDuplicateKey(Throwable error) {
        Throwable e = error;
        while (e != null) {
            if (e instanceof SQLException && "23000".equals(((SQLException) e).getSQLState())) {
                return true;
            }
            e = e.getCause();
        }
        return false;
    }

    /** 部署外置：优先环境变量（便于更换数据库），回退 classpath db.properties */
    private static String envOr(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? fallback : v;
    }

    private DbUtil() {
    }
}