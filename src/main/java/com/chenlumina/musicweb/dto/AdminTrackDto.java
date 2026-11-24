package com.chenlumina.musicweb.dto;

/** 后台曲目行：带 id 供编辑定位 */
public class AdminTrackDto {

    private final Long id;
    private final int trackNo;
    private final String title;
    private final int durationSec;

    public AdminTrackDto(Long id, int trackNo, String title, int durationSec) {
        this.id = id;
        this.trackNo = trackNo;
        this.title = title;
        this.durationSec = durationSec;
    }

    public Long getId() { return id; }
    public int getTrackNo() { return trackNo; }
    public String getTitle() { return title; }
    public int getDurationSec() { return durationSec; }
}