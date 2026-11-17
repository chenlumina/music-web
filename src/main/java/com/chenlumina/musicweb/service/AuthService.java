package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.UserDao;
import com.chenlumina.musicweb.entity.User;
import com.chenlumina.musicweb.util.ValidateException;
import org.mindrot.jbcrypt.BCrypt;

/**
 * 登录业务：规则沿用 music-web 的 AuthService——
 * BCrypt 校验摘要、失败统一文案不泄露账号是否存在、用户不存在时空跑一次 BCrypt 抹平耗时差异。
 * 区别仅在持久层：music-web 用 JPA，本训练工程用 JDBC（UserDao）。
 */
public class AuthService {

    /** 用于用户不存在时空跑 BCrypt，抹平响应耗时差异，防时序探测 */
    private static final String DUMMY_HASH = BCrypt.hashpw("timing", BCrypt.gensalt(4));

    private final UserDao userDao = new UserDao();

    public User login(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty() || password.isEmpty()) {
            throw new ValidateException("用户名或密码错误");
        }
        User user = userDao.findByUsername(username.trim());
        if (user == null) {
            BCrypt.checkpw(password, DUMMY_HASH);
            throw new ValidateException("用户名或密码错误");
        }
        if (!BCrypt.checkpw(password, normalizeHash(user.getPasswordHash()))) {
            throw new ValidateException("用户名或密码错误");
        }
        return user;
    }

    /** jBCrypt 0.4 不认 $2b$ 前缀；$2b 与 $2a 对常规密码算法等价，仅版本标志不同 */
    private static String normalizeHash(String hash) {
        if (hash != null && hash.startsWith("$2b$")) {
            return "$2a$" + hash.substring(4);
        }
        return hash;
    }
}