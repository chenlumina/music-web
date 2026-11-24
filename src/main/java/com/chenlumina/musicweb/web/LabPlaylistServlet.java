package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.service.LabPlaylistService;
import com.chenlumina.musicweb.util.ApiException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** 实验二/三演示控制器：GET /lab/playlists 列表、?action=new 表单、POST 新增（PRG） */
@WebServlet(name = "labPlaylistServlet", urlPatterns = "/lab/playlists")
public class LabPlaylistServlet extends HttpServlet {

    private final LabPlaylistService service = new LabPlaylistService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if ("new".equals(req.getParameter("action"))) {
                req.getRequestDispatcher("/WEB-INF/views/lab/form.jsp").forward(req, resp);
                return;
            }
            List<Playlist> playlists = service.listAll();
            req.setAttribute("playlists", playlists);
            req.getRequestDispatcher("/WEB-INF/views/lab/list.jsp").forward(req, resp);
        } catch (Exception e) {
            getServletContext().log("实验演示列表渲染失败", e);
            req.setAttribute("error", "列表加载失败，请稍后重试");
            req.getRequestDispatcher("/WEB-INF/views/lab/list.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String title = req.getParameter("title");
        String description = req.getParameter("description");
        String coverUrl = req.getParameter("coverUrl");
        String status = req.getParameter("status");
        try {
            long id = service.create(title, description, coverUrl, status);
            String msg = URLEncoder.encode("歌单「" + title.trim() + "」创建成功，编号 " + id,
                    StandardCharsets.UTF_8.name());
            resp.sendRedirect(req.getContextPath() + "/lab/playlists?msg=" + msg);
        } catch (ApiException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("formTitle", title);
            req.setAttribute("formDescription", description);
            req.setAttribute("formCoverUrl", coverUrl);
            req.setAttribute("formStatus", status);
            req.getRequestDispatcher("/WEB-INF/views/lab/form.jsp").forward(req, resp);
        } catch (Exception e) {
            getServletContext().log("实验演示新增失败", e);
            req.setAttribute("error", "保存失败，请稍后重试");
            req.getRequestDispatcher("/WEB-INF/views/lab/form.jsp").forward(req, resp);
        }
    }
}