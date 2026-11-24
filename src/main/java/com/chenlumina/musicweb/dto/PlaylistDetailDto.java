package com.chenlumina.musicweb.dto;

import java.util.List;

/** 歌单详情：封面、标题、描述、标签、曲目列表（开发手册 5.1） */
public class PlaylistDetailDto {

    private final Long id;
    private final String title;
    private final String description;
    private final String coverUrl;
    private final List<String> tags;
    private final int favoriteCount;
    private final List<TrackDto> tracks;

    public PlaylistDetailDto(Long id, String title, String description, String coverUrl,
                             List<String> tags, int favoriteCount, List<TrackDto> tracks) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.coverUrl = coverUrl;
        this.tags = tags;
        this.favoriteCount = favoriteCount;
        this.tracks = tracks;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCoverUrl() { return coverUrl; }
    public List<String> getTags() { return tags; }
    public int getFavoriteCount() { return favoriteCount; }
    public List<TrackDto> getTracks() { return tracks; }
}