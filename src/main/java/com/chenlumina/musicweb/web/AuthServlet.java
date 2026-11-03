package com.chenlumina.musicweb.web;

import com.chenlumina.musicweb.dto.LoginReq;
import com.chenlumina.musicweb.dto.RegisterReq;
import com.chenlumina.musicweb.entity.User;
import com.chenlumina.musicweb.service.AuthService;
import com.chenlumina.musicweb.util.ApiException;
import com.chenlumina.musicweb.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

/** 注册/登录/退出/当前用户；登录态存容器会话（JSESSIONID 默认 HttpOnly） */
@WebServlet(name = "authServlet", urlPatterns = "/api/auth/*")
public class AuthServlet extends HttpServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo();
            switch (path == null ? "" : path) {
                case "/register" -> register(req, resp);
                case "/login" -> login(req, resp);
                case "/logout" -> logout(req, resp);
                default -> throw new ApiException(404, "接口不存在");
            }
        } catch (ApiException e) {
            JsonUtil.error(resp, e.getCode(), e.getMessage());
        } catch (JsonProcessingException e) {
            JsonUtil.error(resp, 400, "参数格式错误");
        } catch (Exception e) {
            getServletContext().log("api/auth 处理失败", e);
            JsonUtil.error(resp, 500, "系统错误");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            if ("/me".equals(req.getPathInfo())) {
                Long userId = currentUserId(req);
                if (userId == null) {
                    throw new ApiException(401, "未登录");
                }
                HttpSession session = req.getSession(false);
                JsonUtil.write(resp, JsonUtil.Resp.ok(Map.of(
                        "id", userId,
                        "username", String.valueOf(session.getAttribute("username")),
                        "role", String.valueOf(session.getAttribute("role")))));
            } else {
                throw new ApiException(404, "接口不存在");
            }
        } catch (ApiException e) {
            JsonUtil.error(resp, e.getCode(), e.getMessage());
        } catch (Exception e) {
            getServletContext().log("api/auth 处理失败", e);
            JsonUtil.error(resp, 500, "系统错误");
        }
    }

    /** 注册成功即自动登录 */
    private void register(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RegisterReq body = MAPPER.readValue(req.getInputStream(), RegisterReq.class);
        User user = authService.register(body.username(), body.password());
        login0(req.getSession(true), user);
        JsonUtil.write(resp, JsonUtil.Resp.ok(profile(user)));
    }

    private void login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginReq body = MAPPER.readValue(req.getInputStream(), LoginReq.class);
        User user = authService.login(body.username(), body.password());
        login0(req.getSession(true), user);
        JsonUtil.write(resp, JsonUtil.Resp.ok(profile(user)));
    }

    private void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        JsonUtil.write(resp, JsonUtil.Resp.ok(null));
    }

    private void login0(HttpSession session, User user) {
        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("role", user.getRole());
        session.setMaxInactiveInterval(7 * 24 * 3600);
    }

    private Map<String, Object> profile(User user) {
        return Map.of("id", user.getId(), "username", user.getUsername(), "role", user.getRole());
    }

    /** 全站取当前登录用户的唯一入口；收藏接口一律用它，不信任前端传入 userId */
    public static Long currentUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        Object v = session == null ? null : session.getAttribute("userId");
        return v == null ? null : (Long) v;
    }
}
