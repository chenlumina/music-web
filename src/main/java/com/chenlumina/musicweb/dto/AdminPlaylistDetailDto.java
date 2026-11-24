package com.chenlumina.musicweb.dto;

import com.chenlumina.musicweb.entity.ShelfStatus;

import java.util.List;

/** 后台歌单编辑详情：标签带 id 供多选框回显，曲目带 id 供维护 */
public class AdminPlaylistDetailDto {

    private final Long id;
    private final String title;
    private final String description;
    private final String coverUrl;
    private final ShelfStatus status;
    private final int favoriteCount;
    private final List<IdName> tags;
    private final List<AdminTrackDto> tracks;

    public AdminPlaylistDetailDto(Long id, String title, String description, String coverUrl,
                                  ShelfStatus status, int favoriteCount,
                                  List<IdName> tags, List<AdminTrackDto> tracks) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.coverUrl = coverUrl;
        this.status = status;
        this.favoriteCount = favoriteCount;
        this.tags = tags;
        this.tracks = tracks;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCoverUrl() { return coverUrl; }
    public ShelfStatus getStatus() { return status; }
    public int getFavoriteCount() { return favoriteCount; }
    public List<IdName> getTags() { return tags; }
    public List<AdminTrackDto> getTracks() { return tracks; }
}