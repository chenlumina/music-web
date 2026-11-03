package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.dto.RecommendUpsertReq;
import com.chenlumina.musicweb.service.AdminRecommendService;
import com.chenlumina.musicweb.util.ApiException;
import com.chenlumina.musicweb.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/** 后台推荐位接口：GET 列表（含停用）、POST 新增、PUT /{id} 编辑 */
@WebServlet(name = "adminRecommendServlet", urlPatterns = "/api/admin/recommendations/*")
public class AdminRecommendServlet extends HttpServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AdminRecommendService service = new AdminRecommendService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonUtil.write(resp, JsonUtil.Resp.ok(service.listAll()));
            } else {
                throw new ApiException(404, "接口不存在");
            }
        } catch (ApiException e) {
            JsonUtil.error(resp, e.getCode(), e.getMessage());
        } catch (Exception e) {
            getServletContext().log("admin/recommendations 查询失败", e);
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
            } else {
                String[] parts = pathInfo.substring(1).split("/");
                if (parts.length == 1) {
                    service.update(parseId(parts[0]), readBody(req));
                    JsonUtil.write(resp, JsonUtil.Resp.ok(null));
                } else {
                    throw new ApiException(404, "接口不存在");
                }
            }
        } catch (ApiException e) {
            JsonUtil.error(resp, e.getCode(), e.getMessage());
        } catch (Exception e) {
            getServletContext().log("admin/recommendations 写入失败", e);
            JsonUtil.error(resp, 500, "系统错误");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doPost(req, resp);
    }

    private RecommendUpsertReq readBody(HttpServletRequest req) throws IOException {
        try {
            return MAPPER.readValue(req.getInputStream(), RecommendUpsertReq.class);
        } catch (JsonProcessingException e) {
            throw new ApiException(400, "参数格式错误");
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
            throw new ApiException(400, "推荐位 id 非法");
        }
    }
}
