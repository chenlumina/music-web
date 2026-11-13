package com.chenlumina.musiclab.web;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 注销：调用 session.invalidate() 让容器销毁会话（JSESSIONID 随之失效），
 * 再重定向回登录页并带上提示。沿用 music-web 的注销语义，只是从 JSON 接口改为页面跳转。
 */
@WebServlet(name = "logoutServlet", urlPatterns = "/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        String msg = URLEncoder.encode("已安全注销", StandardCharsets.UTF_8.name());
        resp.sendRedirect(req.getContextPath() + "/login?msg=" + msg);
    }
}