package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.service.TagService;
import com.chenlumina.musicweb.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** GET /api/tags：标签列表（含各标签在架歌单数） */
@WebServlet(name = "tagServlet", urlPatterns = "/api/tags")
public class TagServlet extends HttpServlet {

    private final TagService tagService = new TagService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonUtil.write(resp, JsonUtil.Resp.ok(tagService.listAll()));
        } catch (Exception e) {
            getServletContext().log("api/tags 处理失败", e);
            JsonUtil.error(resp, 500, "系统错误");
        }
    }
}
