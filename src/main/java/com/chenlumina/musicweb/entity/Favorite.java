package com.chenlumina.musicweb.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 收藏；user_id + playlist_id 数据库唯一约束兜底重复收藏（开发手册 5.3） */
@Entity
@Table(name = "favorites")
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "playlist_id", nullable = false)
    private Long playlistId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getPlaylistId() { return playlistId; }
    public void setPlaylistId(Long playlistId) { this.playlistId = playlistId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
