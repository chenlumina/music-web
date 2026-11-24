package com.chenlumina.musicweb.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** JSON 输出与统一响应封装：所有接口返回 code/message/data（开发手册 7.1） */
public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /** 统一响应体：code=0 成功，其余为错误码 */
    public static final class Resp {

        private final int code;
        private final String message;
        private final Object data;

        private Resp(int code, String message, Object data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }

        public static Resp ok(Object data) {
            return new Resp(0, "ok", data);
        }

        public static Resp error(int code, String message) {
            return new Resp(code, message, null);
        }

        public int getCode() { return code; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
    }

    public static void write(HttpServletResponse resp, Object body) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        MAPPER.writeValue(resp.getWriter(), body);
    }

    /** 业务错误统一 HTTP 200 + body.code，前端只按 code 分流 */
    public static void error(HttpServletResponse resp, int code, String message) throws IOException {
        resp.setStatus(200);
        write(resp, Resp.error(code, message));
    }

    private JsonUtil() {
    }
}