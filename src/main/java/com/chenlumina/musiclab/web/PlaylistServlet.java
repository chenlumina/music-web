package com.chenlumina.musiclab.web;

import com.chenlumina.musiclab.entity.Playlist;
import com.chenlumina.musiclab.service.PlaylistService;
import com.chenlumina.musiclab.util.ValidateException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 实验三重构后的控制器：只做三件事——取参数、调 Service、转发或重定向。
 * GET  /playlist            → 查询列表并转发 list.jsp
 * GET  /playlist?action=new → 转发新增表单
 * POST /playlist            → 新增歌单，成功后重定向回列表（PRG 模式）
 * 编码设置交给 EncodingFilter，未捕获异常交给 web.xml 的 error-page，
 * 因此这里不再重复写 setCharacterEncoding，也不再每个方法都 catch RuntimeException。
 * JSP 移入 /WEB-INF/views 后只能经转发到达，直接访问 URL 会被容器拒绝。
 */
@WebServlet(name = "playlistServlet", urlPatterns = "/playlist")
public class PlaylistServlet extends HttpServlet {

    private final PlaylistService playlistService = new PlaylistService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("new".equals(req.getParameter("action"))) {
            req.getRequestDispatcher("/WEB-INF/views/playlist/form.jsp").forward(req, resp);
            return;
        }

        List<Playlist> playlists = playlistService.listAll();
        req.setAttribute("playlists", playlists);
        req.setAttribute("total", playlists.size());
        req.getRequestDispatcher("/WEB-INF/views/playlist/list.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String title = req.getParameter("title");
        String description = req.getParameter("description");
        String coverUrl = req.getParameter("coverUrl");
        String status = req.getParameter("status");

        try {
            long id = playlistService.create(title, description, coverUrl, status);
            String msg = URLEncoder.encode("歌单「" + title.trim() + "」创建成功，编号 " + id,
                    StandardCharsets.UTF_8.name());
            resp.sendRedirect(req.getContextPath() + "/playlist?msg=" + msg);
        } catch (ValidateException e) {
            // 校验失败：把用户填过的值原样带回表单，避免重填
            req.setAttribute("error", e.getMessage());
            req.setAttribute("formTitle", title);
            req.setAttribute("formDescription", description);
            req.setAttribute("formCoverUrl", coverUrl);
            req.setAttribute("formStatus", status);
            req.getRequestDispatcher("/WEB-INF/views/playlist/form.jsp").forward(req, resp);
        }
    }
}