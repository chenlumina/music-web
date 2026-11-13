package com.chenlumina.musiclab.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接工具：连接信息外置在 classpath 的 db.properties。
 * 所有 DAO 统一从这里拿 Connection，避免每个类各写一套 URL/账号。
 */
public final class DbUtil {

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
        URL = props.getProperty("db.url");
        USER = props.getProperty("db.username");
        PASSWORD = props.getProperty("db.password");
        try {
            // 显式加载驱动，驱动缺失时会在这里直接暴露 ClassNotFoundException
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("MySQL 驱动未找到，请检查 mysql-connector-j 是否在 WEB-INF/lib 中");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private DbUtil() {
    }
}
