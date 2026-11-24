package com.chenlumina.musicweb.entity;

/** 曲目实体：曲目号从 1 开始，整体替换时由服务层重新编号 */
public class Track {

    private Long id;
    private int trackNo;
    private String title;
    /** 时长（秒） */
    private int durationSec;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getTrackNo() { return trackNo; }
    public void setTrackNo(int trackNo) { this.trackNo = trackNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getDurationSec() { return durationSec; }
    public void setDurationSec(int durationSec) { this.durationSec = durationSec; }
}