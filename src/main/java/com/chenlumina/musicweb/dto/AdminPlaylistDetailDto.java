package com.chenlumina.musicweb.dto;

import com.chenlumina.musicweb.entity.ShelfStatus;

import java.util.List;

/** 后台歌单编辑详情：标签带 id 供多选框回显，曲目带 id 供维护 */
public record AdminPlaylistDetailDto(
        Long id,
        String title,
        String description,
        String coverUrl,
        ShelfStatus status,
        int favoriteCount,
        List<IdName> tags,
        List<AdminTrackDto> tracks) {
}
