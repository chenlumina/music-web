package com.chenlumina.musicweb.dto;

/** 后台推荐位行：含启停与跳转歌单标题（停用行前台接口不返回） */
public class AdminRecommendDto {

    private final Long id;
    private final String title;
    private final String imageUrl;
    private final Long playlistId;
    private final String playlistTitle;
    private final int sortOrder;
    private final boolean enabled;

    public AdminRecommendDto(Long id, String title, String imageUrl, Long playlistId,
                             String playlistTitle, int sortOrder, boolean enabled) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.playlistId = playlistId;
        this.playlistTitle = playlistTitle;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
    public Long getPlaylistId() { return playlistId; }
    public String getPlaylistTitle() { return playlistTitle; }
    public int getSortOrder() { return sortOrder; }
    public boolean isEnabled() { return enabled; }
}