package com.chenlumina.musicweb.dto;

/** 标签页展示：标签及歌单数量（开发手册 5.2） */
public record TagDto(Long id, String name, long playlistCount) {
}
