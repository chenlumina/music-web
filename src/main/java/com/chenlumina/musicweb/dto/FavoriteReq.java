package com.chenlumina.musicweb.dto;

/** 收藏请求体：{"playlistId":1} */
public class FavoriteReq {

    private Long playlistId;

    public Long getPlaylistId() { return playlistId; }
    public void setPlaylistId(Long playlistId) { this.playlistId = playlistId; }
}