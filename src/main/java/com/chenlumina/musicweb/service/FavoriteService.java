package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.FavoriteDao;
import com.chenlumina.musicweb.dao.PlaylistDao;
import com.chenlumina.musicweb.dto.FavoriteItemDto;
import com.chenlumina.musicweb.dto.PageDto;
import com.chenlumina.musicweb.entity.Favorite;
import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.entity.ShelfStatus;
import com.chenlumina.musicweb.util.ApiException;
import com.chenlumina.musicweb.util.JpaUtil;
import org.hibernate.exception.ConstraintViolationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 收藏业务：写操作事务边界 + 数据库唯一约束兜底 + 收藏数同步（开发手册 4.3/5.3） */
public class FavoriteService {

    private final FavoriteDao favoriteDao = new FavoriteDao();
    private final PlaylistDao playlistDao = new PlaylistDao();

    /** @return 收藏后的最新收藏数 */
    public int add(Long userId, Long playlistId) {
        try (EntityManager em = JpaUtil.em()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Playlist p = em.find(Playlist.class, playlistId);
                if (p == null || p.getStatus() != ShelfStatus.ONLINE) {
                    throw new ApiException(404, "歌单不存在或已下架");
                }
                if (favoriteDao.exists(em, userId, playlistId)) {
                    throw new ApiException(409, "该歌单已在收藏中");
                }
                Favorite f = new Favorite();
                f.setUserId(userId);
                f.setPlaylistId(playlistId);
                em.persist(f);
                playlistDao.adjustFavoriteCount(em, playlistId, 1);
                tx.commit();
                return p.getFavoriteCount() + 1;
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                if (e instanceof ApiException api) throw api;
                // 并发下的唯一约束兜底（开发手册 15.1：依赖唯一约束与服务层事务）
                if (e instanceof ConstraintViolationException) {
                    throw new ApiException(409, "该歌单已在收藏中");
                }
                throw e;
            }
        }
    }

    public void remove(Long userId, Long playlistId) {
        try (EntityManager em = JpaUtil.em()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                int rows = favoriteDao.delete(em, userId, playlistId);
                if (rows == 0) {
                    throw new ApiException(404, "尚未收藏该歌单");
                }
                playlistDao.adjustFavoriteCount(em, playlistId, -1);
                tx.commit();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                if (e instanceof ApiException api) throw api;
                throw e;
            }
        }
    }

    public PageDto<FavoriteItemDto> myList(Long userId, int page, int size) {
        try (EntityManager em = JpaUtil.em()) {
            List<Favorite> favs = favoriteDao.pageByUser(em, userId, page, size);
            long total = favoriteDao.countByUser(em, userId);
            // 批量取歌单，避免 N+1（开发手册 9.2）
            List<Long> ids = favs.stream().map(Favorite::getPlaylistId).distinct().toList();
            Map<Long, Playlist> playlists = ids.isEmpty() ? Map.of()
                    : em.createQuery("select p from Playlist p where p.id in :ids", Playlist.class)
                            .setParameter("ids", ids)
                            .getResultList().stream()
                            .collect(Collectors.toMap(Playlist::getId, Function.identity()));
            List<FavoriteItemDto> items = favs.stream().map(f -> toDto(f, playlists.get(f.getPlaylistId()))).toList();
            return new PageDto<>(items, total, page, size);
        }
    }

    /** 下架歌单：收藏记录保留，available=false 标记不可用（开发手册 6.2） */
    private FavoriteItemDto toDto(Favorite f, Playlist p) {
        if (p == null) {
            return new FavoriteItemDto(f.getPlaylistId(), null, null, List.of(), 0, false, f.getCreatedAt());
        }
        List<String> tags = p.getTags().stream()
                .sorted(Comparator.comparing(t -> t.getSortOrder()))
                .map(t -> t.getName())
                .toList();
        boolean available = p.getStatus() == ShelfStatus.ONLINE;
        return new FavoriteItemDto(p.getId(), p.getTitle(), p.getCoverUrl(), tags,
                p.getFavoriteCount(), available, f.getCreatedAt());
    }
}
