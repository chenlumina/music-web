package com.chenlumina.musicweb.entity;

/** 曲目：对应 tracks 表 */
public class Track {

    private long id;
    private long playlistId;
    private String title;
    private String artist;
    private int durationSec;
    private int sortOrder;

    /** 时长显示为 m:ss，供 JSP 直接输出 */
    public String getDurationText() {
        int total = Math.max(durationSec, 0);
        return String.format("%d:%02d", total / 60, total % 60);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(long playlistId) {
        this.playlistId = playlistId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getDurationSec() {
        return durationSec;
    }

    public void setDurationSec(int durationSec) {
        this.durationSec = durationSec;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}