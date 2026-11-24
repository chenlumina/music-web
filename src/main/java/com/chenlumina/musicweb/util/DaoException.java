package com.chenlumina.musicweb.util;

/** 数据访问层异常：把受检 SQLException 包装成运行时异常，Servlet 统一按 500 处理 */
public class DaoException extends RuntimeException {

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}