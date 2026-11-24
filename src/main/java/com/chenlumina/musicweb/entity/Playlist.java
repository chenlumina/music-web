package com.chenlumina.musicweb.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 歌单实体：纯 POJO，由 JDBC DAO 组装；对象关系不依赖任何 ORM 框架 */
public class Playlist {

    private Long id;
    private String title;
    private String description;
    private String coverUrl;
    private ShelfStatus status = ShelfStatus.ONLINE;
    /** 收藏数冗余计数（允许准实时，开发手册 9.2） */
    private int favoriteCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Tag> tags = new ArrayList<>();
    private List<Track> tracks = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public ShelfStatus getStatus() { return status; }
    public void setStatus(ShelfStatus status) { this.status = status; }
    public boolean isOnline() { return status == ShelfStatus.ONLINE; }
    public int getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(int favoriteCount) { this.favoriteCount = favoriteCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }
    public List<Track> getTracks() { return tracks; }
    public void setTracks(List<Track> tracks) { this.tracks = tracks; }
}