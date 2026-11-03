package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.util.JsonUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** 后台权限入口：/api/admin/** 仅 ADMIN 角色可访问（开发手册 5.6/9.1，必测用例 7） */
@WebFilter("/api/admin/*")
public class AdminAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        Long userId = AuthServlet.currentUserId(req);
        if (userId == null) {
            JsonUtil.error(resp, 401, "请先登录");
            return;
        }
        if (!"ADMIN".equals(req.getSession(false).getAttribute("role"))) {
            JsonUtil.error(resp, 403, "无权限访问后台");
            return;
        }
        chain.doFilter(request, response);
    }
}
