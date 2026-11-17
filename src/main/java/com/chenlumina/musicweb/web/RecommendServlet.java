package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.service.RecommendService;
import com.chenlumina.musicweb.util.Sessions;
import com.chenlumina.musicweb.util.ValidateException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 推荐：GET 无 action 显示推荐页；action=add/remove 需要登录，操作后回到后台列表。
 */
@WebServlet(name = "recommendServlet", urlPatterns = "/recommend")
public class RecommendServlet extends HttpServlet {

    private final RecommendService recommendService = new RecommendService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("add".equals(action) || "remove".equals(action)) {
            if (Sessions.currentUserId(req) == null) {
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }
            long playlistId = parseId(req.getParameter("playlistId"));
            try {
                if ("add".equals(action)) {
                    recommendService.add(playlistId);
                } else {
                    recommendService.remove(playlistId);
                }
            } catch (ValidateException e) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/playlist");
            return;
        }

        List<Playlist> playlists = recommendService.listOnline();
        req.setAttribute("recommended", playlists);
        req.setAttribute("total", playlists.size());
        req.getRequestDispatcher("/WEB-INF/views/recommend.jsp").forward(req, resp);
    }

    private long parseId(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}