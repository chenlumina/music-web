package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.UserDao;
import com.chenlumina.musicweb.entity.User;
import com.chenlumina.musicweb.util.ApiException;
import com.chenlumina.musicweb.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.mindrot.jbcrypt.BCrypt;

import java.util.regex.Pattern;

/** 注册登录业务；登录失败统一文案不泄露账号是否存在（开发手册 5.5） */
public class AuthService {

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{3,20}$");

    /** 用于用户不存在时空跑 BCrypt，抹平响应耗时差异，防时序探测 */
    private static final String DUMMY_HASH = BCrypt.hashpw("timing", BCrypt.gensalt(4));

    private final UserDao userDao = new UserDao();

    public User register(String username, String password) {
        if (username == null || !USERNAME.matcher(username).matches()) {
            throw new ApiException(400, "用户名须为 3-20 位字母/数字/下划线");
        }
        if (password == null || password.length() < 6 || password.length() > 64) {
            throw new ApiException(400, "密码长度须为 6-64 位");
        }
        try (EntityManager em = JpaUtil.em()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                if (userDao.existsByUsername(em, username)) {
                    throw new ApiException(400, "用户名已被占用");
                }
                User user = new User();
                user.setUsername(username);
                user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(10)));
                user.setRole("USER");
                userDao.insert(em, user);
                tx.commit();
                return user;
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
        }
    }

    public User login(String username, String password) {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            throw new ApiException(401, "用户名或密码错误");
        }
        try (EntityManager em = JpaUtil.em()) {
            User user = userDao.findByUsername(em, username.trim()).orElse(null);
            if (user == null) {
                BCrypt.checkpw(password, DUMMY_HASH);
                throw new ApiException(401, "用户名或密码错误");
            }
            if (!BCrypt.checkpw(password, normalizeHash(user.getPasswordHash()))) {
                throw new ApiException(401, "用户名或密码错误");
            }
            return user;
        }
    }

    /** jBCrypt 0.4 不认 $2b$ 前缀；$2b 与 $2a 对常规密码算法等价，仅版本标志不同 */
    private static String normalizeHash(String hash) {
        if (hash != null && hash.startsWith("$2b$")) {
            return "$2a$" + hash.substring(4);
        }
        return hash;
    }
}
