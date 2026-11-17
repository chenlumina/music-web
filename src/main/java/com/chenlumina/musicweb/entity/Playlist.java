package com.chenlumina.musicweb.entity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/** 歌单：对应 music_web.playlists，前台只展示 ONLINE 状态 */
public class Playlist {

    private long id;
    private String title;
    private String description;
    private String coverUrl;
    private String status;
    private LocalDateTime createdAt;

    public Playlist() {
    }

    public Playlist(String title, String description, String coverUrl, String status) {
        this.title = title;
        this.description = description;
        this.coverUrl = coverUrl;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** 供 JSTL fmt:formatDate 使用：LocalDateTime 转 java.util.Date */
    public Date getCreatedAtDate() {
        return createdAt == null ? null
                : Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant());
    }

    /** 与原始项目保持一致：只有 ONLINE 才在前台可见 */
    public boolean isOnline() {
        return "ONLINE".equals(status);
    }
}
