package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.service.FavoriteService;
import com.chenlumina.musicweb.util.Sessions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** 我的收藏：需登录，列出当前用户收藏的歌单 */
@WebServlet(name = "myFavoritesServlet", urlPatterns = "/my-favorites")
public class MyFavoritesServlet extends HttpServlet {

    private final FavoriteService favoriteService = new FavoriteService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Long userId = Sessions.currentUserId(req);
        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        List<Playlist> playlists = favoriteService.listMine(userId);
        req.setAttribute("playlists", playlists);
        req.setAttribute("total", playlists.size());
        req.setAttribute("myFavMode", Boolean.TRUE);
        req.getRequestDispatcher("/WEB-INF/views/my-favorites.jsp").forward(req, resp);
    }
}