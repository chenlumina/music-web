package com.chenlumina.musicweb.util;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 封面上传存储：默认写入 CATALINA_BASE/uploads，可用环境变量 UPLOAD_DIR 覆盖。
 * 文件名统一为 up-<uuid>.<ext>，由 CoverServlet 以 /covers/* 提供访问。
 */
public final class UploadStore {

    private static final Path DIR = resolveDir();

    private UploadStore() {
    }

    private static Path resolveDir() {
        String env = System.getenv("UPLOAD_DIR");
        if (env != null && !env.trim().isEmpty()) {
            return Paths.get(env.trim());
        }
        String base = System.getProperty("catalina.base");
        if (base != null && !base.isEmpty()) {
            return Paths.get(base, "uploads");
        }
        return Paths.get(System.getProperty("java.io.tmpdir"), "music-web-uploads");
    }

    public static Path dir() {
        try {
            Files.createDirectories(DIR);
        } catch (IOException e) {
            throw new IllegalStateException("创建上传目录失败", e);
        }
        return DIR;
    }

    public static String save(Part part, String ext) throws IOException {
        String name = "up-" + UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try (InputStream in = part.getInputStream()) {
            Files.copy(in, dir().resolve(name), StandardCopyOption.REPLACE_EXISTING);
        }
        return name;
    }

    public static Path find(String name) {
        if (name == null || !name.matches("[A-Za-z0-9._-]+")) {
            return null;
        }
        Path base = dir();
        Path file = base.resolve(name).normalize();
        if (!file.startsWith(base)) {
            return null;
        }
        return Files.exists(file) ? file : null;
    }
}