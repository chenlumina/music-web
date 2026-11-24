package com.chenlumina.musicweb.dto;

import java.util.List;

/** 歌单列表卡片字段：封面、标题、标签、收藏数（开发手册 5.1） */
public class PlaylistCardDto {

    private final Long id;
    private final String title;
    private final String coverUrl;
    private final List<String> tags;
    private final int favoriteCount;

    public PlaylistCardDto(Long id, String title, String coverUrl, List<String> tags, int favoriteCount) {
        this.id = id;
        this.title = title;
        this.coverUrl = coverUrl;
        this.tags = tags;
        this.favoriteCount = favoriteCount;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getCoverUrl() { return coverUrl; }
    public List<String> getTags() { return tags; }
    public int getFavoriteCount() { return favoriteCount; }
}