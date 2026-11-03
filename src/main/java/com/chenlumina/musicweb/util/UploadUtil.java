package com.chenlumina.musicweb.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/** 封面上传目录：db.properties 可外置配置 upload.dir，缺省落在应用服务器目录（开发手册 11 章配置外置） */
public final class UploadUtil {

    private static volatile Path coverDir;

    public static Path coverDir() throws IOException {
        Path dir = coverDir;
        if (dir == null) {
            synchronized (UploadUtil.class) {
                if (coverDir == null) {
                    coverDir = build();
                }
                dir = coverDir;
            }
        }
        return dir;
    }

    private static Path build() throws IOException {
        String configured = null;
        try (InputStream in = UploadUtil.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                Properties p = new Properties();
                p.load(in);
                configured = p.getProperty("upload.dir");
            }
        }
        String base = configured;
        if (base == null || base.isBlank()) {
            base = System.getProperty("catalina.base", System.getProperty("java.io.tmpdir"));
        }
        Path dir = Paths.get(base.trim(), "music-web-uploads", "covers");
        Files.createDirectories(dir);
        return dir;
    }

    private UploadUtil() {
    }
}
