package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.service.FavoriteService;
import com.chenlumina.musicweb.service.PlaylistService;
import com.chenlumina.musicweb.service.TagService;
import com.chenlumina.musicweb.util.Sessions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 前台只读入口：列表仅展示上架歌单；详情仅可查看上架歌单，无需登录。
 */
@WebServlet(name = "frontServlet", urlPatterns = "/front")
public class FrontServlet extends HttpServlet {

    private final PlaylistService playlistService = new PlaylistService();
    private final FavoriteService favoriteService = new FavoriteService();
    private final TagService tagService = new TagService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("detail".equals(req.getParameter("action"))) {
            long id = parseId(req.getParameter("id"));
            Playlist playlist = playlistService.getById(id);
            if (playlist == null || !playlist.isOnline()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            Long userId = Sessions.currentUserId(req);
            req.setAttribute("playlist", playlist);
            req.setAttribute("favorited", favoriteService.isFavorited(userId, id));
            req.setAttribute("playlistTags", tagService.tagsOf(id));
            req.setAttribute("frontMode", Boolean.TRUE);
            req.getRequestDispatcher("/WEB-INF/views/playlist/detail.jsp").forward(req, resp);
            return;
        }

        long tagId = parseId(req.getParameter("tag"));
        List<Playlist> playlists = tagId > 0
                ? tagService.listOnlineByTag(tagId)
                : playlistService.listOnline();
        req.setAttribute("playlists", playlists);
        req.setAttribute("total", playlists.size());
        req.setAttribute("allTags", tagService.listAll());
        req.setAttribute("activeTag", tagId > 0 ? tagId : null);
        req.setAttribute("frontMode", Boolean.TRUE);
        req.getRequestDispatcher("/WEB-INF/views/playlist/list.jsp").forward(req, resp);
    }

    private long parseId(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}