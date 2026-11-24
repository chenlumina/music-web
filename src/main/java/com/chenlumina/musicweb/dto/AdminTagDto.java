package com.chenlumina.musicweb.dto;

/** 后台标签行：playlistCount 为绑定歌单数（含下架） */
public class AdminTagDto {

    private final Long id;
    private final String name;
    private final int sortOrder;
    private final long playlistCount;

    public AdminTagDto(Long id, String name, int sortOrder, long playlistCount) {
        this.id = id;
        this.name = name;
        this.sortOrder = sortOrder;
        this.playlistCount = playlistCount;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public int getSortOrder() { return sortOrder; }
    public long getPlaylistCount() { return playlistCount; }
}