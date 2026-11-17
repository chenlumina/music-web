package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.service.PlaylistService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 实验五前台只读入口：真实业务规则——仅 ONLINE 歌单可见，无需登录。
 * 与后台 /playlist（全量、需登录）区分；下架操作生效后前台立即不可见。
 */
@WebServlet(name = "frontServlet", urlPatterns = "/front")
public class FrontServlet extends HttpServlet {

    private final PlaylistService playlistService = new PlaylistService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Playlist> playlists = playlistService.listOnline();
        req.setAttribute("playlists", playlists);
        req.setAttribute("total", playlists.size());
        req.setAttribute("frontMode", Boolean.TRUE);
        req.getRequestDispatcher("/WEB-INF/views/playlist/list.jsp").forward(req, resp);
    }
}