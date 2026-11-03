package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.service.RecommendService;
import com.chenlumina.musicweb.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** GET /api/recommend：推荐占位，返回后台启用配置；空列表由前端降级（开发手册 5.4） */
@WebServlet(name = "recommendServlet", urlPatterns = "/api/recommend")
public class RecommendServlet extends HttpServlet {

    private final RecommendService recommendService = new RecommendService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonUtil.write(resp, JsonUtil.Resp.ok(recommendService.listEnabled()));
        } catch (Exception e) {
            getServletContext().log("api/recommend 处理失败", e);
            JsonUtil.error(resp, 500, "系统错误");
        }
    }
}
