package com.chenlumina.musicweb.util;

/** DAO 层统一异常：包装 SQLException 并保留原因链，由 web.xml 的 error-page 兜底展示 */
public class DaoException extends RuntimeException {

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}