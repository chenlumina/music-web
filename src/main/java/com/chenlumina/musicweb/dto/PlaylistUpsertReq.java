package com.chenlumina.musicweb.dto;

import java.util.List;

/** 歌单新增/编辑请求体：tracks 全量提交，后端整体替换 */
public class PlaylistUpsertReq {

    private String title;
    private String description;
    private String coverUrl;
    private List<Long> tagIds;
    private List<TrackReq> tracks;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public List<Long> getTagIds() { return tagIds; }
    public void setTagIds(List<Long> tagIds) { this.tagIds = tagIds; }
    public List<TrackReq> getTracks() { return tracks; }
    public void setTracks(List<TrackReq> tracks) { this.tracks = tracks; }
}