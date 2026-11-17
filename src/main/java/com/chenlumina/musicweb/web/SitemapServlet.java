package com.chenlumina.musicweb.web;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/** 站点地图：输出首页、前台、登录、注册等公开地址 */
@WebServlet(name = "sitemapServlet", urlPatterns = "/sitemap.xml")
public class SitemapServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String base = req.getScheme() + "://" + req.getServerName()
                + (req.getServerPort() == 80 || req.getServerPort() == 443 ? "" : ":" + req.getServerPort())
                + req.getContextPath();
        resp.setContentType("application/xml;charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
            for (String path : new String[]{"/", "/front", "/login", "/register"}) {
                out.println("  <url><loc>" + base + path + "</loc></url>");
            }
            out.println("</urlset>");
        }
    }
}