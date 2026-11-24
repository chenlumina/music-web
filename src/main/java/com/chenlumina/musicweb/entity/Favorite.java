package com.chenlumina.musicweb.entity;

import java.time.LocalDateTime;

/** 收藏实体：user_id + playlist_id 由数据库唯一约束兜底重复收藏（开发手册 5.3） */
public class Favorite {

    private Long id;
    private Long userId;
    private Long playlistId;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getPlaylistId() { return playlistId; }
    public void setPlaylistId(Long playlistId) { this.playlistId = playlistId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}