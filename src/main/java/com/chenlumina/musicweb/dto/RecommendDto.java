package com.chenlumina.musicweb.dto;

/** 推荐位展示：标题、图片、跳转歌单（开发手册 5.4） */
public record RecommendDto(Long id, String title, String imageUrl, Long playlistId) {
}
