package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.dto.PageDto;
import com.chenlumina.musicweb.dto.PlaylistCardDto;
import com.chenlumina.musicweb.entity.ShelfStatus;
import com.chenlumina.musicweb.service.PlaylistService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** 站点地图（开发手册 9.3）：首页 + 全部在架歌单详情页，供搜索引擎发现 */
@WebServlet(name = "sitemapServlet", urlPatterns = "/sitemap.xml")
public class SitemapServlet extends HttpServlet {

    private static final int PAGE_SIZE = 50;

    private final PlaylistService playlistService = new PlaylistService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        String base = req.getScheme() + "://" + req.getServerName()
                + (req.getServerPort() == 80 || req.getServerPort() == 443 ? "" : ":" + req.getServerPort())
                + req.getContextPath();
        xml.append("  <url><loc>").append(base).append("/</loc></url>\n");

        int page = 1;
        while (true) {
            PageDto<PlaylistCardDto> dto = playlistService.search(ShelfStatus.ONLINE, null, page, PAGE_SIZE);
            for (PlaylistCardDto p : dto.list()) {
                xml.append("  <url><loc>").append(base).append("/playlist?id=").append(p.id())
                        .append("</loc></url>\n");
            }
            if ((long) page * PAGE_SIZE >= dto.total()) {
                break;
            }
            page++;
        }
        xml.append("</urlset>\n");

        resp.setContentType("application/xml");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.getOutputStream().write(xml.toString().getBytes(StandardCharsets.UTF_8));
    }
}
