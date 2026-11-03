package com.chenlumina.musicweb.dto;

import java.time.LocalDateTime;
import java.util.List;

/** 我的收藏条目；歌单下架时 available=false，记录保留但标记不可用（开发手册 6.2） */
public record FavoriteItemDto(
        Long playlistId,
        String title,
        String coverUrl,
        List<String> tags,
        int favoriteCount,
        boolean available,
        LocalDateTime favoritedAt) {
}
