package com.chenlumina.musicweb.dto;

/** 收藏请求体；只收 playlistId，userId 一律取自会话，防越权（开发手册 9.1） */
public record FavoriteReq(Long playlistId) {
}
