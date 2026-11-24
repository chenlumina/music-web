package com.chenlumina.musicweb.entity;

/** 用户实体：密码字段只存 BCrypt 摘要，禁止明文（开发手册 5.5） */
public class User {

    private Long id;
    private String username;
    private String passwordHash;
    /** USER=注册用户 ADMIN=运营管理员 */
    private String role;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}