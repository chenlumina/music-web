package com.chenlumina.musicweb.dto;

import java.time.LocalDateTime;
import java.util.List;

/** 我的收藏条目；歌单下架时 available=false，记录保留但标记不可用（开发手册 6.2） */
public class FavoriteItemDto {

    private final Long playlistId;
    private final String title;
    private final String coverUrl;
    private final List<String> tags;
    private final int favoriteCount;
    private final boolean available;
    private final LocalDateTime favoritedAt;

    public FavoriteItemDto(Long playlistId, String title, String coverUrl, List<String> tags,
                           int favoriteCount, boolean available, LocalDateTime favoritedAt) {
        this.playlistId = playlistId;
        this.title = title;
        this.coverUrl = coverUrl;
        this.tags = tags;
        this.favoriteCount = favoriteCount;
        this.available = available;
        this.favoritedAt = favoritedAt;
    }

    public Long getPlaylistId() { return playlistId; }
    public String getTitle() { return title; }
    public String getCoverUrl() { return coverUrl; }
    public List<String> getTags() { return tags; }
    public int getFavoriteCount() { return favoriteCount; }
    public boolean isAvailable() { return available; }
    public LocalDateTime getFavoritedAt() { return favoritedAt; }
}