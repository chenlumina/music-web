package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.dto.PlaylistDetailDto;
import com.chenlumina.musicweb.dto.TrackDto;
import com.chenlumina.musicweb.service.PlaylistService;
import com.chenlumina.musicweb.util.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 歌单详情页（服务端渲染，开发手册 5.1/9.3：标题描述完整、可索引、下架返回 404）
 *  GET /playlist?id={id}
 */
@WebServlet(name = "playlistPageServlet", urlPatterns = "/playlist")
public class PlaylistPageServlet extends HttpServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    /** 无自定义封面的歌单按首个标签映射内置封面，与前端 app.js 的映射保持一致 */
    private static final Map<String, String> TAG_COVER = Map.of(
            "雨声", "cover-rain.jpg", "溪流", "cover-stream.jpg", "鸟鸣", "cover-bird.jpg",
            "森林", "cover-forest.jpg", "海浪", "cover-waves.jpg", "篝火", "cover-campfire.jpg",
            "白噪音", "cover-fog.jpg", "夜晚", "cover-night.jpg");

    private final PlaylistService playlistService = new PlaylistService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            long id = parseId(req.getParameter("id"));
            Optional<PlaylistDetailDto> opt = playlistService.detail(id);
            if (!opt.isPresent()) {
                renderError(req, resp, 404, "歌单不存在或已下架");
                return;
            }
            PlaylistDetailDto p = opt.get();
            // 用 Map 组装视图模型，JSP/EL 只依赖 getter 之外的通用 Map 访问
            Map<String, Object> vm = new LinkedHashMap<>();
            vm.put("id", p.getId());
            vm.put("title", p.getTitle());
            vm.put("description", p.getDescription());
            vm.put("tags", p.getTags());
            vm.put("favoriteCount", p.getFavoriteCount());
            List<Map<String, Object>> tracks = new ArrayList<>();
            for (TrackDto t : p.getTracks()) {
                int s = Math.max(0, t.getDurationSec());
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("trackNo", t.getTrackNo());
                row.put("title", t.getTitle());
                row.put("dur", (s / 60) + ":" + String.format("%02d", s % 60));
                tracks.add(row);
            }
            vm.put("tracks", tracks);
            req.setAttribute("p", vm);
            req.setAttribute("cover", resolveCover(req, p));
            req.setAttribute("jsonLd", buildJsonLd(p));
            RequestDispatcher view = req.getRequestDispatcher("/playlist.jsp");
            view.forward(req, resp);
        } catch (ApiException e) {
            renderError(req, resp, e.getCode(), e.getMessage());
        } catch (Exception e) {
            getServletContext().log("playlist 页面渲染失败", e);
            renderError(req, resp, 500, "系统错误");
        }
    }

    private String resolveCover(HttpServletRequest req, PlaylistDetailDto p) {
        if (p.getCoverUrl() != null && !p.getCoverUrl().isBlank()) {
            return p.getCoverUrl();
        }
        String file = "cover-fog.jpg";
        for (String tag : p.getTags()) {
            String mapped = TAG_COVER.get(tag);
            if (mapped != null) {
                file = mapped;
                break;
            }
        }
        return req.getContextPath() + "/assets/img/" + file;
    }

    /** 结构化数据（MusicPlaylist）；转义 < 防 </script> 逃逸 */
    private String buildJsonLd(PlaylistDetailDto p) {
        Map<String, Object> ld = new LinkedHashMap<>();
        ld.put("@context", "https://schema.org");
        ld.put("@type", "MusicPlaylist");
        ld.put("name", p.getTitle());
        ld.put("description", p.getDescription() == null ? "" : p.getDescription());
        ld.put("numTracks", p.getTracks().size());
        try {
            return MAPPER.writeValueAsString(ld).replace("<", "\\u003c");
        } catch (Exception e) {
            return "{}";
        }
    }

    private void renderError(HttpServletRequest req, HttpServletResponse resp, int code, String msg)
            throws IOException, ServletException {
        resp.setStatus(code);
        req.setAttribute("code", code);
        req.setAttribute("msg", msg);
        req.getRequestDispatcher("/error.jsp").forward(req, resp);
    }

    private long parseId(String raw) {
        try {
            long id = Long.parseLong(raw);
            if (id < 1) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException | NullPointerException e) {
            throw new ApiException(400, "歌单 id 非法");
        }
    }
}