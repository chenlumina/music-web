package com.chenlumina.musicweb.dto;

import com.chenlumina.musicweb.entity.ShelfStatus;

import java.time.LocalDateTime;
import java.util.List;

/** 后台歌单行：状态、曲目数、更新时间等运营字段（开发手册 5.6） */
public class AdminPlaylistRowDto {

    private final Long id;
    private final String title;
    private final String coverUrl;
    private final ShelfStatus status;
    private final List<String> tags;
    private final int trackCount;
    private final int favoriteCount;
    private final LocalDateTime updatedAt;

    public AdminPlaylistRowDto(Long id, String title, String coverUrl, ShelfStatus status,
                               List<String> tags, int trackCount, int favoriteCount, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.coverUrl = coverUrl;
        this.status = status;
        this.tags = tags;
        this.trackCount = trackCount;
        this.favoriteCount = favoriteCount;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getCoverUrl() { return coverUrl; }
    public ShelfStatus getStatus() { return status; }
    public List<String> getTags() { return tags; }
    public int getTrackCount() { return trackCount; }
    public int getFavoriteCount() { return favoriteCount; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}