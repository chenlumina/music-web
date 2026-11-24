package com.chenlumina.musicweb.dto;

/** 推荐位新增/编辑请求体：playlistId 可空（纯文案占位），enabled 可空（默认启用） */
public class RecommendUpsertReq {

    private String title;
    private String imageUrl;
    private Long playlistId;
    private Integer sortOrder;
    private Boolean enabled;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Long getPlaylistId() { return playlistId; }
    public void setPlaylistId(Long playlistId) { this.playlistId = playlistId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}