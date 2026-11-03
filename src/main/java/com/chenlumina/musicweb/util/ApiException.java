package com.chenlumina.musicweb.util;

/** 业务异常：携带错误码（开发手册 7.1 错误码表），Servlet 层统一转 JSON 响应 */
public class ApiException extends RuntimeException {

    private final int code;

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
