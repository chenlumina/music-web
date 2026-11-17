package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.service.AdminService;
import com.chenlumina.musicweb.service.RecommendService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 后台管理仪表盘：统计数据 + 全量歌单；由 AdminAuthFilter 保证仅 ADMIN 可达 */
@WebServlet(name = "adminServlet", urlPatterns = "/admin")
public class AdminServlet extends HttpServlet {

    private final AdminService adminService = new AdminService();
    private final RecommendService recommendService = new RecommendService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("stats", adminService.stats());
        req.setAttribute("playlists", adminService.allPlaylists());
        req.setAttribute("recommendedIds", recommendService.ids());
        req.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(req, resp);
    }
}