package com.chenlumina.musicweb.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/** 会话读取工具：全站取当前登录用户只走这里，避免各处重复判空 */
public final class Sessions {

    private Sessions() {
    }

    public static Long currentUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("userId");
        return value instanceof Long ? (Long) value : null;
    }
}