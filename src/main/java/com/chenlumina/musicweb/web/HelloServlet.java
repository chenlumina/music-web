package com.chenlumina.musicweb.web;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 实验一环境验证 Servlet：/hello，演示映射、参数、响应编码与生命周期日志（不属于产品导航） */
@WebServlet(name = "helloServlet", urlPatterns = "/hello")
public class HelloServlet extends HttpServlet {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void init() throws ServletException {
        log("【生命周期】HelloServlet init() 执行，实例已创建");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        log("【生命周期】HelloServlet service() 执行，method=" + req.getMethod());
        super.service(req, resp);
    }

    @Override
    public void destroy() {
        log("【生命周期】HelloServlet destroy() 执行，实例即将销毁");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        String name = req.getParameter("name");
        String who = (name == null || name.trim().isEmpty()) ? "同学" : name.trim();
        String serverTime = LocalDateTime.now().format(FORMATTER);

        try (PrintWriter out = resp.getWriter()) {
            out.println("<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset=\"UTF-8\">");
            out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
            out.println("<title>Hello Servlet · music-web</title>");
            out.println("<link rel=\"stylesheet\" href=\"" + req.getContextPath() + "/assets/theme.css\">");
            out.println("<style>.box{max-width:680px;margin:110px auto;padding:38px 36px;border-radius:var(--r-panel)}"
                    + ".box h1{margin:0 0 14px;font-size:28px}.box p{margin:8px 0;color:var(--txt2)}"
                    + ".box .time{color:var(--acc);font-size:20px;margin-top:18px}</style></head><body>");
            out.println("<main class=\"box glass\"><h1>你好，" + escape(who) + "</h1>");
            out.println("<p>这是 JavaEE8 环境验证 Servlet（@WebServlet(\"/hello\")）。</p>");
            out.println("<p>请求参数 name 可选：不传时回退为“同学”。</p>");
            out.println("<p class=\"time num\">当前服务器时间：" + serverTime + "</p>");
            out.println("<p><a class=\"btn btn-ghost btn-sm\" href=\"" + req.getContextPath() + "/\">返回首页</a></p>");
            out.println("</main></body></html>");
        }
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}