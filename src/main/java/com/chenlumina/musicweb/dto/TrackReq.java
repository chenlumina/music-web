package com.chenlumina.musicweb.dto;

/** 后台曲目提交项：曲目号由后端按顺序重排，时长可空（默认 0） */
public class TrackReq {

    private String title;
    private Integer durationSec;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getDurationSec() { return durationSec; }
    public void setDurationSec(Integer durationSec) { this.durationSec = durationSec; }
}