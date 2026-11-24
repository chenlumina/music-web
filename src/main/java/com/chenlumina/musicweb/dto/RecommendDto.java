package com.chenlumina.musicweb.dto;

/** 前台推荐位：仅启用的配置，playlistId 可为 null（纯展示） */
public class RecommendDto {

    private final Long id;
    private final String title;
    private final String imageUrl;
    private final Long playlistId;

    public RecommendDto(Long id, String title, String imageUrl, Long playlistId) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.playlistId = playlistId;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
    public Long getPlaylistId() { return playlistId; }
}