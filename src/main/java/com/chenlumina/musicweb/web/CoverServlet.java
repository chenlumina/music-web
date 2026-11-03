package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.util.UploadUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;

/** 封面静态读取：文件名由服务端 UUID 生成并白名单校验，天然防路径穿越（开发手册 4.3/9.1） */
@WebServlet(name = "coverServlet", urlPatterns = "/covers/*")
public class CoverServlet extends HttpServlet {

    private static final Pattern NAME = Pattern.compile(
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|jpeg|png|webp)");
    private static final Map<String, String> MIME = Map.of(
            "jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png", "webp", "image/webp");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getPathInfo() == null ? "" : req.getPathInfo().substring(1);
        if (!NAME.matcher(name).matches()) {
            resp.sendError(404);
            return;
        }
        Path file = UploadUtil.coverDir().resolve(name);
        if (!Files.isRegularFile(file)) {
            resp.sendError(404);
            return;
        }
        String ext = name.substring(name.lastIndexOf('.') + 1);
        resp.setContentType(MIME.get(ext));
        resp.setHeader("Cache-Control", "public, max-age=86400");
        Files.copy(file, resp.getOutputStream());
    }
}
