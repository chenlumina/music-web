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

/**
 * 实验一：Servlet 生命周期与请求响应。
 * 访问 /hello 或 /hello?name=张亮，返回服务器时间与一段自定义信息。
 */
@WebServlet(name = "helloServlet", urlPatterns = "/hello")
public class HelloServlet extends HttpServlet {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void init() throws ServletException {
        // 容器创建 Servlet 实例后只调用一次
        log("【生命周期】HelloServlet init() 执行，实例已创建");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 每次请求都会先进入 service()，再由它分派到 doGet/doPost
        log("【生命周期】HelloServlet service() 执行，method=" + req.getMethod());
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 请求/响应字符编码已由 EncodingFilter 统一设置；直接写 HTML 仍需声明内容类型
        resp.setContentType("text/html;charset=UTF-8");

        String name = req.getParameter("name");
        String who = (name == null || name.trim().isEmpty()) ? "同学" : name.trim();
        String serverTime = LocalDateTime.now().format(FORMATTER);
        String contextPath = req.getContextPath();

        try (PrintWriter out = resp.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang=\"zh-CN\"><head><meta charset=\"UTF-8\">");
            out.println("<title>Hello · 自然音乐歌单</title>");
            out.println("<link rel=\"icon\" href=\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M4 20c9 1 16-5.5 16-16-8.5-1-15.5 4-16 12 0 1.4.2 2.8 0 4z' fill='%237ce3ab'/%3E%3C/svg%3E\">");
            out.println("<link rel=\"stylesheet\" href=\"" + contextPath + "/assets/theme.css\">");
            out.println("</head><body>");
            out.println("<main style=\"max-width:720px;margin:0 auto;padding:96px 24px\">");
            out.println("<div class=\"glass\" style=\"border-radius:22px;padding:40px\">");
            out.println("<p class=\"badge\">Servlet 生命周期演示</p>");
            out.println("<h1 style=\"margin:18px 0 10px;font-size:30px;font-weight:600\">你好，" + who + "</h1>");
            out.println("<p style=\"color:var(--txt2);line-height:1.9;margin:0 0 22px\">"
                    + "这里是 music-web 的第一个 Servlet，由 Tomcat9 容器创建、调用与销毁。</p>");
            out.println("<div style=\"display:grid;gap:12px;font-size:14px\">");
            out.println("<div style=\"display:flex;justify-content:space-between;border-bottom:1px solid var(--line);padding-bottom:10px\">"
                    + "<span style=\"color:var(--txt3)\">当前服务器时间</span><span class=\"num\">" + serverTime + "</span></div>");
            out.println("<div style=\"display:flex;justify-content:space-between;border-bottom:1px solid var(--line);padding-bottom:10px\">"
                    + "<span style=\"color:var(--txt3)\">上下文路径</span><span>" + contextPath + "</span></div>");
            out.println("<div style=\"display:flex;justify-content:space-between;border-bottom:1px solid var(--line);padding-bottom:10px\">"
                    + "<span style=\"color:var(--txt3)\">请求方式</span><span>" + req.getMethod() + "</span></div>");
            out.println("<div style=\"display:flex;justify-content:space-between\">"
                    + "<span style=\"color:var(--txt3)\">运行容器</span><span>"
                    + getServletContext().getServerInfo() + "</span></div>");
            out.println("</div>");
            out.println("<p style=\"margin:26px 0 0\"><a class=\"btn btn-ghost btn-sm\" href=\""
                    + contextPath + "/\">返回首页</a></p>");
            out.println("</div></main></body></html>");
        }
    }

    @Override
    public void destroy() {
        // 容器卸载应用时调用，用来释放资源
        log("【生命周期】HelloServlet destroy() 执行，实例即将销毁");
    }
}
