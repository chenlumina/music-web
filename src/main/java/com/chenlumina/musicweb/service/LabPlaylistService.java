package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.PlaylistDao;
import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.entity.ShelfStatus;
import com.chenlumina.musicweb.util.ApiException;
import com.chenlumina.musicweb.util.DbUtil;

import java.util.List;

/** 实验二/三演示用业务：歌单列表与新增，复用 JDBC DAO 与事务工具（不属于产品接口） */
public class LabPlaylistService {

    private final PlaylistDao playlistDao = new PlaylistDao();

    public List<Playlist> listAll() {
        return DbUtil.withConnection(conn -> playlistDao.searchAdmin(conn, null, null, 1, 100));
    }

    public long create(String title, String description, String coverUrl, String status) {
        String safeTitle = title == null ? "" : title.trim();
        if (safeTitle.isEmpty()) {
            throw new ApiException(400, "歌单标题不能为空");
        }
        if (safeTitle.length() > 100) {
            throw new ApiException(400, "歌单标题不能超过 100 个字符");
        }
        String safeDesc = description == null ? "" : description.trim();
        if (safeDesc.length() > 500) {
            throw new ApiException(400, "歌单描述不能超过 500 个字符");
        }
        String safeCover = coverUrl == null ? "" : coverUrl.trim();
        if (safeCover.length() > 255) {
            throw new ApiException(400, "封面地址过长");
        }
        String safeStatus = status == null || status.trim().isEmpty()
                ? "ONLINE" : status.trim().toUpperCase();
        if (!"ONLINE".equals(safeStatus) && !"OFFLINE".equals(safeStatus)) {
            throw new ApiException(400, "歌单状态只能是 ONLINE 或 OFFLINE");
        }
        return DbUtil.inTransaction(conn -> {
            Playlist p = new Playlist();
            p.setTitle(safeTitle);
            p.setDescription(safeDesc.isEmpty() ? null : safeDesc);
            p.setCoverUrl(safeCover.isEmpty() ? null : safeCover);
            p.setStatus(ShelfStatus.valueOf(safeStatus));
            p.setFavoriteCount(0);
            return playlistDao.insert(conn, p);
        });
    }
}