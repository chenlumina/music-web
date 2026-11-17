package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.UserDao;
import com.chenlumina.musicweb.entity.User;
import com.chenlumina.musicweb.util.ValidateException;
import org.mindrot.jbcrypt.BCrypt;

import java.util.regex.Pattern;

/**
 * 认证业务：BCrypt 校验摘要、失败统一文案不泄露账号是否存在、
 * 用户不存在时空跑一次 BCrypt 抹平耗时差异；注册时校验用户名格式与唯一性。
 */
public class AuthService {

    /** 用于用户不存在时空跑 BCrypt，抹平响应耗时差异，防时序探测 */
    private static final String DUMMY_HASH = BCrypt.hashpw("timing", BCrypt.gensalt(4));

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{3,20}$");

    private final UserDao userDao = new UserDao();

    /** 注册：用户名 3-20 位字母/数字/下划线，密码 6-64 位，用户名唯一；成功返回新用户 */
    public User register(String username, String password) {
        String safeName = username == null ? "" : username.trim();
        if (!USERNAME.matcher(safeName).matches()) {
            throw new ValidateException("用户名须为 3-20 位字母、数字或下划线");
        }
        if (password == null || password.length() < 6 || password.length() > 64) {
            throw new ValidateException("密码长度须为 6-64 位");
        }
        if (userDao.existsByUsername(safeName)) {
            throw new ValidateException("用户名已被占用");
        }
        User user = new User();
        user.setUsername(safeName);
        user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt(10)));
        user.setRole("USER");
        user.setId(userDao.insert(user));
        return user;
    }

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