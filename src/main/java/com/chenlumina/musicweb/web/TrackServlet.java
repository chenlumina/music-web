package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.service.TrackService;
import com.chenlumina.musicweb.util.Sessions;
import com.chenlumina.musicweb.util.ValidateException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 曲目增删：需登录，操作后回到歌单编辑页 */
@WebServlet(name = "trackServlet", urlPatterns = "/track")
public class TrackServlet extends HttpServlet {

    private final TrackService trackService = new TrackService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (Sessions.currentUserId(req) == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if ("delete".equals(req.getParameter("action"))) {
            long id = parseId(req.getParameter("id"));
            long playlistId = parseId(req.getParameter("playlistId"));
            trackService.remove(id);
            resp.sendRedirect(req.getContextPath() + "/playlist?action=edit&id=" + playlistId);
            return;
        }
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (Sessions.currentUserId(req) == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        long playlistId = parseId(req.getParameter("playlistId"));
        try {
            trackService.add(playlistId,
                    req.getParameter("trackTitle"),
                    req.getParameter("trackArtist"),
                    parseInt(req.getParameter("trackDuration")));
        } catch (ValidateException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/playlist?action=edit&id=" + playlistId);
    }

    private long parseId(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int parseInt(String raw) {
        try {
            return Math.max(Integer.parseInt(raw), 0);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}