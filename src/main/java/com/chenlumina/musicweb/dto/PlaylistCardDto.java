package com.chenlumina.musicweb.dto;

import java.util.List;

/** 歌单列表卡片字段：封面、标题、标签、收藏数（开发手册 5.1） */
public record PlaylistCardDto(
        Long id,
        String title,
        String coverUrl,
        List<String> tags,
        int favoriteCount) {
}
