package com.chenlumina.musicweb.dto;

import java.util.List;

/** 歌单新增/编辑请求：tracks 全量提交，后端整体替换 */
public record PlaylistUpsertReq(
        String title,
        String description,
        String coverUrl,
        List<Long> tagIds,
        List<TrackReq> tracks) {
}
