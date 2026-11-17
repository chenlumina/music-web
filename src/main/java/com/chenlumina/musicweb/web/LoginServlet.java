package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.entity.User;
import com.chenlumina.musicweb.service.AuthService;
import com.chenlumina.musicweb.util.ValidateException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 实验三登录控制器：
 * GET  /login → 已登录直接进列表，未登录转发登录页
 * POST /login → AuthService 校验，成功后把 userId/username/role 写入 HttpSession 并重定向（PRG）
 * 请求/响应编码由 EncodingFilter 统一设置，这里不再重复。
 */
@WebServlet(name = "loginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            resp.sendRedirect(req.getContextPath() + "/playlist");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        try {
            User user = authService.login(username, password);
            HttpSession session = req.getSession(true);
            // 会话属性名与 music-web 保持一致，全站取登录态只认这三个键
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());
            session.setMaxInactiveInterval(7 * 24 * 3600);
            resp.sendRedirect(req.getContextPath() + "/playlist");
        } catch (ValidateException e) {
            // 失败统一文案；只回显用户名，密码绝不回显
            req.setAttribute("error", e.getMessage());
            req.setAttribute("username", username);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }
}