package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.service.FavoriteService;
import com.chenlumina.musicweb.service.PlaylistService;
import com.chenlumina.musicweb.service.RecommendService;
import com.chenlumina.musicweb.service.TagService;
import com.chenlumina.musicweb.service.TrackService;
import com.chenlumina.musicweb.util.Sessions;
import com.chenlumina.musicweb.util.UploadStore;
import com.chenlumina.musicweb.util.ValidateException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
public class PlaylistServlet extends HttpServlet {

    private final PlaylistService playlistService = new PlaylistService();
    private final FavoriteService favoriteService = new FavoriteService();
    private final TagService tagService = new TagService();
    private final TrackService trackService = new TrackService();
    private final RecommendService recommendService = new RecommendService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("new".equals(req.getParameter("action"))) {
            req.setAttribute("allTags", tagService.listAll());
            req.getRequestDispatcher("/WEB-INF/views/playlist/form.jsp").forward(req, resp);
            return;
        }

        if ("edit".equals(req.getParameter("action"))) {
            long id = parseId(req.getParameter("id"));
            Playlist playlist = playlistService.getById(id);
            if (playlist == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            req.setAttribute("playlist", playlist);
            req.setAttribute("allTags", tagService.listAll());
            req.setAttribute("selectedTagIds", tagService.idsOf(id));
            req.setAttribute("tracks", trackService.list(id));
            req.getRequestDispatcher("/WEB-INF/views/playlist/edit.jsp").forward(req, resp);
            return;
        }

        if ("detail".equals(req.getParameter("action"))) {
            long id = parseId(req.getParameter("id"));
            Playlist playlist = playlistService.getById(id);
            if (playlist == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            req.setAttribute("playlist", playlist);
            req.setAttribute("favorited",
                    favoriteService.isFavorited(Sessions.currentUserId(req), id));
            req.setAttribute("playlistTags", tagService.tagsOf(id));
            req.setAttribute("playlistTracks", trackService.list(id));
            req.setAttribute("adminMode", Boolean.TRUE);
            req.getRequestDispatcher("/WEB-INF/views/playlist/detail.jsp").forward(req, resp);
            return;
        }

        int page = parsePage(req.getParameter("page"));
        int size = 6;
        long total = playlistService.countAll();
        int totalPages = (int) Math.max(1, (total + size - 1) / size);
        if (page > totalPages) {
            page = totalPages;
        }
        List<Playlist> playlists = playlistService.listPage(page, size);
        req.setAttribute("playlists", playlists);
        req.setAttribute("total", total);
        req.setAttribute("page", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("recommendedIds", recommendService.ids());
        req.getRequestDispatcher("/WEB-INF/views/playlist/list.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String title = req.getParameter("title");
        String description = req.getParameter("description");
        String coverUrl = req.getParameter("coverUrl");
        String status = req.getParameter("status");

        if ("update".equals(req.getParameter("action"))) {
            long id = parseId(req.getParameter("id"));
            // 审查发现：不存在的 id 更新 0 行仍会提示成功，先做存在性校验
            if (playlistService.getById(id) == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            String uploaded = saveUploadedCover(req);
            if (uploaded != null) {
                coverUrl = uploaded;
            }
            try {
                playlistService.update(id, title, description, coverUrl, status);
                tagService.assign(id, toIds(req.getParameterValues("tagIds")));
                String msg = URLEncoder.encode("歌单「" + title.trim() + "」更新成功",
                        StandardCharsets.UTF_8.name());
                resp.sendRedirect(req.getContextPath() + "/playlist?msg=" + msg);
            } catch (ValidateException e) {
                req.setAttribute("error", e.getMessage());
                Playlist echo = new Playlist(title, description, coverUrl, status);
                echo.setId(id);
                req.setAttribute("playlist", echo);
                req.setAttribute("allTags", tagService.listAll());
                req.setAttribute("selectedTagIds", toIds(req.getParameterValues("tagIds")));
                req.getRequestDispatcher("/WEB-INF/views/playlist/edit.jsp").forward(req, resp);
            }
            return;
        }

        String uploaded = saveUploadedCover(req);
        if (uploaded != null) {
            coverUrl = uploaded;
        }
        try {
            long id = playlistService.create(title, description, coverUrl, status);
            tagService.assign(id, toIds(req.getParameterValues("tagIds")));
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
            req.setAttribute("allTags", tagService.listAll());
            req.setAttribute("selectedTagIds", toIds(req.getParameterValues("tagIds")));
            req.getRequestDispatcher("/WEB-INF/views/playlist/form.jsp").forward(req, resp);
        }
    }

    /** multipart 表单若带 coverFile 且扩展名合法，则保存并返回文件名；否则返回 null */
    private String saveUploadedCover(HttpServletRequest req) throws IOException, ServletException {
        String contentType = req.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("multipart/")) {
            return null;
        }
        Part part = req.getPart("coverFile");
        if (part == null || part.getSize() == 0) {
            return null;
        }
        String submitted = part.getSubmittedFileName();
        String ext = "";
        if (submitted != null && submitted.lastIndexOf('.') >= 0) {
            ext = submitted.substring(submitted.lastIndexOf('.') + 1).toLowerCase();
        }
        if (!ext.matches("jpg|jpeg|png|gif|webp")) {
            return null;
        }
        return UploadStore.save(part, ext);
    }

    private List<Long> toIds(String[] raw) {
        List<Long> ids = new ArrayList<>();
        if (raw != null) {
            for (String s : raw) {
                try {
                    ids.add(Long.parseLong(s));
                } catch (NumberFormatException ignored) {
                    // 非数字标签值直接忽略
                }
            }
        }
        return ids;
    }

    private long parseId(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int parsePage(String raw) {
        try {
            return Math.max(Integer.parseInt(raw), 1);
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}