package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.util.ApiException;
import com.chenlumina.musicweb.util.JsonUtil;
import com.chenlumina.musicweb.util.UploadUtil;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** 封面上传：限制格式/大小并校验文件头，文件名服务端生成，防路径穿越（开发手册 4.3/9.1） */
@WebServlet(name = "adminUploadServlet", urlPatterns = "/api/admin/upload")
@MultipartConfig(maxFileSize = 2 * 1024 * 1024)
public class AdminUploadServlet extends HttpServlet {

    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_SIZE = 2 * 1024 * 1024;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Part part = req.getPart("file");
            if (part == null || part.getSize() == 0) {
                throw new ApiException(400, "请选择要上传的图片");
            }
            if (part.getSize() > MAX_SIZE) {
                throw new ApiException(400, "图片不能超过 2MB");
            }
            String ext = extOf(part.getSubmittedFileName());
            if (!ALLOWED.contains(ext)) {
                throw new ApiException(400, "仅支持 jpg/png/webp 图片");
            }
            byte[] data = part.getInputStream().readAllBytes();
            checkMagic(data, ext);
            String name = UUID.randomUUID() + "." + ext;
            Path target = UploadUtil.coverDir().resolve(name);
            Files.write(target, data);
            JsonUtil.write(resp, JsonUtil.Resp.ok(Map.of(
                    "url", req.getContextPath() + "/covers/" + name)));
        } catch (ApiException e) {
            JsonUtil.error(resp, e.getCode(), e.getMessage());
        } catch (Exception e) {
            getServletContext().log("封面上传失败", e);
            JsonUtil.error(resp, 500, "上传失败");
        }
    }

    private String extOf(String submittedName) {
        if (submittedName == null) {
            return "";
        }
        int dot = submittedName.lastIndexOf('.');
        if (dot < 0 || dot == submittedName.length() - 1) {
            return "";
        }
        return submittedName.substring(dot + 1).toLowerCase();
    }

    /** 文件头校验：防改后缀伪装（jpg=FFD8FF，png=89504E47，webp=RIFF…WEBP） */
    private void checkMagic(byte[] head, String ext) {
        boolean ok = switch (ext) {
            case "jpg", "jpeg" -> head.length >= 3 && (head[0] & 0xFF) == 0xFF
                    && (head[1] & 0xFF) == 0xD8 && (head[2] & 0xFF) == 0xFF;
            case "png" -> head.length >= 8 && (head[0] & 0xFF) == 0x89 && head[1] == 0x50
                    && head[2] == 0x4E && head[3] == 0x47;
            case "webp" -> head.length >= 12 && head[0] == 'R' && head[1] == 'I' && head[2] == 'F'
                    && head[3] == 'F' && head[8] == 'W' && head[9] == 'E' && head[10] == 'B' && head[11] == 'P';
            default -> false;
        };
        if (!ok) {
            throw new ApiException(400, "文件内容不是有效图片");
        }
    }
}
