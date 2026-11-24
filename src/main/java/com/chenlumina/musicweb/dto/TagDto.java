package com.chenlumina.musicweb.dto;

/** 前台标签：playlistCount 为该标签下的在架歌单数 */
public class TagDto {

    private final Long id;
    private final String name;
    private final long playlistCount;

    public TagDto(Long id, String name, long playlistCount) {
        this.id = id;
        this.name = name;
        this.playlistCount = playlistCount;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public long getPlaylistCount() { return playlistCount; }
}