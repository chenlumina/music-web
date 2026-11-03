package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.dto.PlaylistDetailDto;
import com.chenlumina.musicweb.service.PlaylistService;
import com.chenlumina.musicweb.util.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

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
            var opt = playlistService.detail(id);
            if (opt.isEmpty()) {
                renderError(req, resp, 404, "歌单不存在或已下架");
                return;
            }
            PlaylistDetailDto p = opt.get();
            // Tomcat 10.1 的 EL 5.0 不支持 record 组件取值，这里组装 Map 视图模型供 JSP 使用
            Map<String, Object> vm = new LinkedHashMap<>();
            vm.put("id", p.id());
            vm.put("title", p.title());
            vm.put("description", p.description());
            vm.put("tags", p.tags());
            vm.put("favoriteCount", p.favoriteCount());
            vm.put("tracks", p.tracks().stream()
                    .map(t -> {
                        int s = Math.max(0, t.durationSec());
                        return Map.of("trackNo", t.trackNo(), "title", t.title(),
                                "dur", (s / 60) + ":" + String.format("%02d", s % 60));
                    })
                    .toList());
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
        if (p.coverUrl() != null && !p.coverUrl().isBlank()) {
            return p.coverUrl();
        }
        String file = p.tags().stream().map(TAG_COVER::get).filter(f -> f != null).findFirst().orElse("cover-fog.jpg");
        return req.getContextPath() + "/assets/img/" + file;
    }

    /** 结构化数据（MusicPlaylist）；转义 < 防 </script> 逃逸 */
    private String buildJsonLd(PlaylistDetailDto p) {
        Map<String, Object> ld = new LinkedHashMap<>();
        ld.put("@context", "https://schema.org");
        ld.put("@type", "MusicPlaylist");
        ld.put("name", p.title());
        ld.put("description", p.description() == null ? "" : p.description());
        ld.put("numTracks", p.tracks().size());
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
