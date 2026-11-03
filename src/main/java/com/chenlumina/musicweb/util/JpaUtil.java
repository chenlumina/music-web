package com.chenlumina.musicweb.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/** EntityManagerFactory 全局单例；连接配置从 db.properties 注入（配置外置，不硬编码密钥） */
public final class JpaUtil {

    private static final EntityManagerFactory EMF = build();

    private static EntityManagerFactory build() {
        Map<String, Object> props = new HashMap<>();
        Properties p = new Properties();
        try (InputStream in = JpaUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("classpath 下找不到 db.properties");
            }
            p.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("读取 db.properties 失败", e);
        }
        props.put("jakarta.persistence.jdbc.url", p.getProperty("db.url"));
        props.put("jakarta.persistence.jdbc.user", p.getProperty("db.username"));
        props.put("jakarta.persistence.jdbc.password", p.getProperty("db.password"));
        props.put("hibernate.show_sql", p.getProperty("db.showSql", "false"));
        // 驼峰属性名自动映射蛇形列名：coverUrl -> cover_url
        props.put("hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        return Persistence.createEntityManagerFactory("music-web", props);
    }

    public static EntityManager em() {
        return EMF.createEntityManager();
    }

    private JpaUtil() {
    }
}
