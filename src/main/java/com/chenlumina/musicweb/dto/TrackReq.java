package com.chenlumina.musicweb.dto;

/** 曲目保存项：按数组顺序编号（trackNo 由后端生成） */
public record TrackReq(String title, Integer durationSec) {
}
