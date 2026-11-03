package com.chenlumina.musicweb.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** JSON 输出与统一响应封装：所有接口返回 code/message/data（开发手册 7.1） */
public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public record Resp(int code, String message, Object data) {
        public static Resp ok(Object data) {
            return new Resp(0, "ok", data);
        }

        public static Resp error(int code, String message) {
            return new Resp(code, message, null);
        }
    }

    public static void write(HttpServletResponse resp, Object body) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        MAPPER.writeValue(resp.getWriter(), body);
    }

    public static void error(HttpServletResponse resp, int code, String message) throws IOException {
        resp.setStatus(200);
        write(resp, Resp.error(code, message));
    }

    private JsonUtil() {
    }
}
