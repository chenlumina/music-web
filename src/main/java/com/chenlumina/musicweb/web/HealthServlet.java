package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.util.DbUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;

/**
 * 实验七部署加固：健康检查端点，公开只读。
 * DB 可达且连接有效返回 200 "OK db=up"，否则 503 "FAIL db=down"，供负载/部署脚本探活。
 */
@WebServlet(name = "healthServlet", urlPatterns = "/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        boolean dbOk = false;
        try (Connection conn = DbUtil.getConnection()) {
            dbOk = conn.isValid(2);
        } catch (Exception e) {
            log("健康检查：数据库连接失败", e);
        }
        resp.setContentType("text/plain;charset=UTF-8");
        resp.setStatus(dbOk ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        resp.getWriter().println(dbOk ? "OK db=up" : "FAIL db=down");
    }
}