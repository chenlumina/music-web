package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.dto.FavoriteReq;
import com.chenlumina.musicweb.service.FavoriteService;
import com.chenlumina.musicweb.util.ApiException;
import com.chenlumina.musicweb.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/** POST /api/favorites、DELETE /api/favorites/{playlistId}、GET /api/me/favorites（全部须登录） */
@WebServlet(name = "favoriteServlet", urlPatterns = {"/api/favorites/*", "/api/me/favorites"})
public class FavoriteServlet extends HttpServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final FavoriteService favoriteService = new FavoriteService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Long userId = requireLogin(req);
            FavoriteReq body = MAPPER.readValue(req.getInputStream(), FavoriteReq.class);
            if (body.playlistId() == null || body.playlistId() < 1) {
                throw new ApiException(400, "playlistId 非法");
            }
            int count = favoriteService.add(userId, body.playlistId());
            JsonUtil.write(resp, JsonUtil.Resp.ok(Map.of(
                    "playlistId", body.playlistId(),
                    "favoriteCount", count)));
        } catch (ApiException e) {
            JsonUtil.error(resp, e.getCode(), e.getMessage());
        } catch (Exception e) {
            getServletContext().log("api/favorites 处理失败", e);
            JsonUtil.error(resp, 500, "系统错误");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Long userId = requireLogin(req);
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.length() < 2) {
                throw new ApiException(400, "缺少 playlistId");
            }
            long playlistId;
            try {
                playlistId = Long.parseLong(pathInfo.substring(1));
            } catch (NumberFormatException e) {
                throw new ApiException(400, "playlistId 非法");
            }
            favoriteService.remove(userId, playlistId);
            JsonUtil.write(resp, JsonUtil.Resp.ok(null));
        } catch (ApiException e) {
            JsonUtil.error(resp, e.getCode(), e.getMessage());
        } catch (Exception e) {
            getServletContext().log("api/favorites 处理失败", e);
            JsonUtil.error(resp, 500, "系统错误");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Long userId = requireLogin(req);
            int page = parsePositive(req.getParameter("page"), 1);
            int size = parsePositive(req.getParameter("size"), 10);
            if (size > 50) {
                throw new ApiException(400, "size 不能超过 50");
            }
            JsonUtil.write(resp, JsonUtil.Resp.ok(favoriteService.myList(userId, page, size)));
        } catch (ApiException e) {
            JsonUtil.error(resp, e.getCode(), e.getMessage());
        } catch (Exception e) {
            getServletContext().log("api/me/favorites 处理失败", e);
            JsonUtil.error(resp, 500, "系统错误");
        }
    }

    private Long requireLogin(HttpServletRequest req) {
        Long userId = AuthServlet.currentUserId(req);
        if (userId == null) {
            throw new ApiException(401, "请先登录");
        }
        return userId;
    }

    private int parsePositive(String raw, int defaultValue) {
        if (raw == null || raw.isBlank()) {
            return defaultValue;
        }
        try {
            int v = Integer.parseInt(raw);
            if (v < 1) {
                throw new NumberFormatException();
            }
            return v;
        } catch (NumberFormatException e) {
            throw new ApiException(400, "page/size 必须为正整数");
        }
    }
}
