package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.util.DbUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 实验七部署加固：/health 健康检查，数据库可用返回 200，否则 503（不属于产品导航） */
@WebServlet(name = "healthServlet", urlPatterns = "/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        boolean dbOk = false;
        try {
            dbOk = DbUtil.withConnection(conn -> conn.isValid(2));
        } catch (Exception e) {
            log("健康检查：数据库连接失败", e);
        }
        resp.setContentType("text/plain;charset=UTF-8");
        resp.setStatus(dbOk ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        resp.getWriter().println(dbOk ? "OK db=up" : "FAIL db=down");
    }
}