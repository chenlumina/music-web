package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.util.UploadStore;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** 上传封面读取：/covers/<filename>，带路径穿越防护 */
@WebServlet(name = "coverServlet", urlPatterns = "/covers/*")
public class CoverServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getPathInfo();
        if (name != null && name.startsWith("/")) {
            name = name.substring(1);
        }
        Path file = UploadStore.find(name);
        if (file == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String lower = name.toLowerCase();
        String contentType;
        if (lower.endsWith(".png")) {
            contentType = "image/png";
        } else if (lower.endsWith(".gif")) {
            contentType = "image/gif";
        } else if (lower.endsWith(".webp")) {
            contentType = "image/webp";
        } else {
            contentType = "image/jpeg";
        }
        resp.setContentType(contentType);
        resp.setHeader("Cache-Control", "max-age=86400");
        Files.copy(file, resp.getOutputStream());
    }
}