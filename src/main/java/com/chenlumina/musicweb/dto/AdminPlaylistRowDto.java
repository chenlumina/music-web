package com.chenlumina.musicweb.dto;

import com.chenlumina.musicweb.entity.ShelfStatus;

import java.time.LocalDateTime;
import java.util.List;

/** 后台歌单行：状态、曲目数、更新时间等运营字段（开发手册 5.6） */
public record AdminPlaylistRowDto(
        Long id,
        String title,
        String coverUrl,
        ShelfStatus status,
        List<String> tags,
        int trackCount,
        int favoriteCount,
        LocalDateTime updatedAt) {
}
