package com.chenlumina.musicweb.dto;

/** 后台推荐位行：含启停与跳转歌单标题（停用行前台接口不返回） */
public record AdminRecommendDto(
        Long id,
        String title,
        String imageUrl,
        Long playlistId,
        String playlistTitle,
        int sortOrder,
        boolean enabled) {
}
