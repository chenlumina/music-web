package com.chenlumina.musicweb.dto;

import java.util.List;

/** 歌单详情：封面、标题、描述、标签、曲目列表（开发手册 5.1） */
public record PlaylistDetailDto(
        Long id,
        String title,
        String description,
        String coverUrl,
        List<String> tags,
        int favoriteCount,
        List<TrackDto> tracks) {
}
