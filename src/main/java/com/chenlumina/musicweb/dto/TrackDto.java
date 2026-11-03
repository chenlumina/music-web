package com.chenlumina.musicweb.dto;

/** 曲目仅展示序号、曲名、时长；播放为占位（开发手册 5.1） */
public record TrackDto(int trackNo, String title, int durationSec) {
}
