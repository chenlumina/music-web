package com.chenlumina.musicweb.dto;

/** 推荐位新增/编辑请求：playlistId 可空（纯文案占位），enabled 可空（默认启用） */
public record RecommendUpsertReq(
        String title,
        String imageUrl,
        Long playlistId,
        Integer sortOrder,
        Boolean enabled) {
}
