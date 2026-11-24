package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.FavoriteDao;
import com.chenlumina.musicweb.dao.PlaylistDao;
import com.chenlumina.musicweb.dto.FavoriteItemDto;
import com.chenlumina.musicweb.dto.PageDto;
import com.chenlumina.musicweb.entity.Favorite;
import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.entity.ShelfStatus;
import com.chenlumina.musicweb.entity.Tag;
import com.chenlumina.musicweb.util.ApiException;
import com.chenlumina.musicweb.util.DbUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 收藏业务：写操作事务边界 + 数据库唯一约束兜底 + 收藏数同步（开发手册 4.3/5.3） */
public class FavoriteService {

    private final FavoriteDao favoriteDao = new FavoriteDao();
    private final PlaylistDao playlistDao = new PlaylistDao();

    /** @return 收藏后的最新收藏数 */
    public int add(Long userId, Long playlistId) {
        try {
            return DbUtil.inTransaction(conn -> {
                Playlist p = playlistDao.findById(conn, playlistId);
                if (p == null || p.getStatus() != ShelfStatus.ONLINE) {
                    throw new ApiException(404, "歌单不存在或已下架");
                }
                if (favoriteDao.exists(conn, userId, playlistId)) {
                    throw new ApiException(409, "该歌单已在收藏中");
                }
                favoriteDao.insert(conn, userId, playlistId);
                playlistDao.adjustFavoriteCount(conn, playlistId, 1);
                return p.getFavoriteCount() + 1;
            });
        } catch (ApiException e) {
            throw e;
        } catch (RuntimeException e) {
            // 并发下的唯一约束兜底（开发手册 15.1：依赖唯一约束与服务层事务）
            if (DbUtil.isDuplicateKey(e)) {
                throw new ApiException(409, "该歌单已在收藏中");
            }
            throw e;
        }
    }

    public void remove(Long userId, Long playlistId) {
        DbUtil.inTransaction(conn -> {
            int rows = favoriteDao.delete(conn, userId, playlistId);
            if (rows == 0) {
                throw new ApiException(404, "尚未收藏该歌单");
            }
            playlistDao.adjustFavoriteCount(conn, playlistId, -1);
            return null;
        });
    }

    public PageDto<FavoriteItemDto> myList(Long userId, int page, int size) {
        return DbUtil.withConnection(conn -> {
            List<Favorite> favs = favoriteDao.pageByUser(conn, userId, page, size);
            long total = favoriteDao.countByUser(conn, userId);
            List<Long> ids = favs.stream().map(Favorite::getPlaylistId).distinct().collect(Collectors.toList());
            // 批量取歌单与标签，避免 N+1（开发手册 9.2）
            Map<Long, Playlist> playlists = new HashMap<>();
            for (Playlist p : playlistDao.findByIds(conn, ids)) {
                playlists.put(p.getId(), p);
            }
            Map<Long, List<Tag>> tagMap = playlistDao.tagsOf(conn, ids);
            List<FavoriteItemDto> items = new ArrayList<>();
            for (Favorite f : favs) {
                items.add(toDto(f, playlists.get(f.getPlaylistId()), tagMap));
            }
            return new PageDto<>(items, total, page, size);
        });
    }

    /** 下架歌单：收藏记录保留，available=false 标记不可用（开发手册 6.2） */
    private FavoriteItemDto toDto(Favorite f, Playlist p, Map<Long, List<Tag>> tagMap) {
        if (p == null) {
            return new FavoriteItemDto(f.getPlaylistId(), null, null,
                    Collections.emptyList(), 0, false, f.getCreatedAt());
        }
        List<String> tags = new ArrayList<>();
        for (Tag t : tagMap.getOrDefault(p.getId(), Collections.emptyList())) {
            tags.add(t.getName());
        }
        boolean available = p.getStatus() == ShelfStatus.ONLINE;
        return new FavoriteItemDto(p.getId(), p.getTitle(), p.getCoverUrl(), tags,
                p.getFavoriteCount(), available, f.getCreatedAt());
    }
}