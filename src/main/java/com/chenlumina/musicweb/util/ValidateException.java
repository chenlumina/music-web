package com.chenlumina.musicweb.util;

/** 业务校验失败：由 Service 抛出，Servlet 捕获后回显到表单 */
public class ValidateException extends RuntimeException {

    public ValidateException(String message) {
        super(message);
    }
}
