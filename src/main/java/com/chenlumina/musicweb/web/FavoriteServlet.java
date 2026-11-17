package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.service.FavoriteService;
import com.chenlumina.musicweb.util.Sessions;
import com.chenlumina.musicweb.util.ValidateException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 收藏开关：需登录；切换后回到该歌单前台详情 */
@WebServlet(name = "favoriteServlet", urlPatterns = "/favorite")
public class FavoriteServlet extends HttpServlet {

    private final FavoriteService favoriteService = new FavoriteService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long userId = Sessions.currentUserId(req);
        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        long playlistId = parseId(req.getParameter("playlistId"));
        try {
            favoriteService.toggle(userId, playlistId);
        } catch (ValidateException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/front?action=detail&id=" + playlistId);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doGet(req, resp);
    }

    private long parseId(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}