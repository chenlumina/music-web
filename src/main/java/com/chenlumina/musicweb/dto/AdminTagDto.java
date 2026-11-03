package com.chenlumina.musicweb.dto;

/** 后台标签行：绑定歌单数含下架歌单（运营视角，开发手册 5.6） */
public record AdminTagDto(Long id, String name, int sortOrder, long playlistCount) {
}
