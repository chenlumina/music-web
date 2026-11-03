package com.chenlumina.musicweb.dto;

/** 后台曲目行：带 id 供编辑定位 */
public record AdminTrackDto(Long id, int trackNo, String title, int durationSec) {
}
