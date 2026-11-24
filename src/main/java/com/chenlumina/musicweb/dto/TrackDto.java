package com.chenlumina.musicweb.dto;

/** 歌单详情曲目：前台展示用，时长单位秒 */
public class TrackDto {

    private final int trackNo;
    private final String title;
    private final int durationSec;

    public TrackDto(int trackNo, String title, int durationSec) {
        this.trackNo = trackNo;
        this.title = title;
        this.durationSec = durationSec;
    }

    public int getTrackNo() { return trackNo; }
    public String getTitle() { return title; }
    public int getDurationSec() { return durationSec; }
}