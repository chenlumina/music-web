package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.dto.PlaylistUpsertReq;
import com.chenlumina.musicweb.service.AdminPlaylistService;
import com.chenlumina.musicweb.util.ApiException;
import com.chenlumina.musicweb.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/** 后台歌单接口（/api/admin/**，AdminAuthFilter 已保证管理员身份）
 *  GET  /api/admin/playlists?q=&status=&page=&size=  列表（搜索/状态筛选/分页）
 *  POST /api/admin/playlists                         新增（默认下架）
 *  GET  /api/admin/playlists/{id}                    编辑详情
 *  PUT  /api/admin/playlists/{id}                    编辑保存（标签、曲目整体替换）
 *  POST /api/admin/playlists/{id}/status             上下架
 */
@WebServlet(name = "adminPlaylistServlet", urlPatterns = "/api/admin/playlists/*")
public class AdminPlaylistServlet extends HttpServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AdminPlaylistService service = new AdminPlaylistService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                String q = req.getParameter("q");
                String status = req.getParameter("status");
                int page = parsePositive(req.getParameter("page"), 1);
                int size = parsePositive(req.getParameter("size"), 10);
                if (size > 50) {
                    throw new ApiException(400, "size 不能超过 50");
                }
                JsonUtil.write(resp, JsonUtil.Resp.ok(service.search(q, status, page, size)));
            } else {
                String sub = pathInfo.substring(1);
                if (sub.contains("/")) {
                    throw new ApiException(404, "接口不存在");
                }
                JsonUtil.write(resp, JsonUtil.Resp.ok(service.detail(parseId(sub))));
            }
        } catch (ApiException e) {
            JsonUtil.error(resp, e.getCode(), e.getMessage());
        } catch (Exception e) {
            getServletContext().log("admin/playlists 查询失败", e);
            JsonUtil.error(resp, 500, "系统错误");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                Long id = service.create(readBody(req));
                JsonUtil.write(resp, JsonUtil.Resp.ok(Map.of("id", id)));
                return;
            }
            String[] parts = pathInfo.substring(1).split("/");
            if (parts.length == 2 && "status".equals(parts[1])) {
                JsonNode node = MAPPER.readTree(req.getInputStream());
                service.changeStatus(parseId(parts[0]), node.path("status").asText(null));
                JsonUtil.write(resp, JsonUtil.Resp.ok(null));
            } else if (parts.length == 1) {
                service.update(parseId(parts[0]), readBody(req));
                JsonUtil.write(resp, JsonUtil.Resp.ok(null));
            } else {
                throw new ApiException(404, "接口不存在");
            }
        } catch (ApiException e) {
            JsonUtil.error(resp, e.getCode(), e.getMessage());
        } catch (Exception e) {
            getServletContext().log("admin/playlists 写入失败", e);
            JsonUtil.error(resp, 500, "系统错误");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // PUT 与 POST /{id} 等价，均作编辑保存
        doPost(req, resp);
    }

    private PlaylistUpsertReq readBody(HttpServletRequest req) throws IOException {
        try {
            return MAPPER.readValue(req.getInputStream(), PlaylistUpsertReq.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(400, "参数格式错误");
        }
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

    private long parseId(String raw) {
        try {
            long id = Long.parseLong(raw);
            if (id < 1) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException e) {
            throw new ApiException(400, "歌单 id 非法");
        }
    }
}
