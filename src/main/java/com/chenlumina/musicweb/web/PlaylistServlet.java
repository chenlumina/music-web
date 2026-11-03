package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.dto.PageDto;
import com.chenlumina.musicweb.entity.ShelfStatus;
import com.chenlumina.musicweb.service.PlaylistService;
import com.chenlumina.musicweb.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** GET /api/playlists?tag=&page=&size= 与 GET /api/playlists/{id} */
@WebServlet(name = "playlistServlet", urlPatterns = "/api/playlists/*")
public class PlaylistServlet extends HttpServlet {

    private final PlaylistService playlistService = new PlaylistService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                list(req, resp);
            } else {
                detail(pathInfo.substring(1), resp);
            }
        } catch (IllegalArgumentException e) {
            JsonUtil.error(resp, 400, e.getMessage());
        } catch (Exception e) {
            getServletContext().log("api/playlists 处理失败", e);
            JsonUtil.error(resp, 500, "系统错误");
        }
    }

    /** 前台固定只展示上架歌单（开发手册 4.3） */
    private void list(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String tag = req.getParameter("tag");
        int page = parsePositive(req.getParameter("page"), 1);
        int size = parsePositive(req.getParameter("size"), 10);
        if (size > 50) {
            throw new IllegalArgumentException("size 不能超过 50");
        }
        PageDto<?> data = playlistService.search(ShelfStatus.ONLINE, tag, page, size);
        JsonUtil.write(resp, JsonUtil.Resp.ok(data));
    }

    private void detail(String idStr, HttpServletResponse resp) throws IOException {
        long id = parseId(idStr);
        var detail = playlistService.detail(id);
        if (detail.isEmpty()) {
            JsonUtil.error(resp, 404, "歌单不存在或已下架");
            return;
        }
        JsonUtil.write(resp, JsonUtil.Resp.ok(detail.get()));
    }

    /** page/size 缺省走默认值；非法值统一抛 400 */
    private int parsePositive(String raw, int defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        int v = Integer.parseInt(raw);
        if (v < 1) {
            throw new IllegalArgumentException("page/size 必须为正整数");
        }
        return v;
    }

    private long parseId(String raw) {
        try {
            long id = Long.parseLong(raw);
            if (id < 1) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("歌单 id 非法");
        }
    }
}
