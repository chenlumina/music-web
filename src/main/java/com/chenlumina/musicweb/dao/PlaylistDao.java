package com.chenlumina.musicweb.dao;

import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.entity.ShelfStatus;
import com.chenlumina.musicweb.entity.Track;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 歌单 JPA 查询：分页、标签筛选、详情（开发手册 4.2 Repository 层） */
public class PlaylistDao {

    /** 标签筛选 + 上架状态过滤 + 服务端分页；tagName 为空查全部 */
    public List<Playlist> search(EntityManager em, ShelfStatus status, String tagName, int page, int size) {
        String where = "where p.status = :status";
        if (tagName != null && !tagName.isBlank()) {
            where += " and t.name = :tag";
        }
        String jpql = "select distinct p from Playlist p left join p.tags t "
                + where + " order by p.id desc";
        var q = em.createQuery(jpql, Playlist.class)
                .setParameter("status", status)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size);
        if (tagName != null && !tagName.isBlank()) {
            q.setParameter("tag", tagName.trim());
        }
        return q.getResultList();
    }

    public long count(EntityManager em, ShelfStatus status, String tagName) {
        String where = "where p.status = :status";
        if (tagName != null && !tagName.isBlank()) {
            where += " and t.name = :tag";
        }
        String jpql = "select count(distinct p) from Playlist p left join p.tags t " + where;
        var q = em.createQuery(jpql, Long.class).setParameter("status", status);
        if (tagName != null && !tagName.isBlank()) {
            q.setParameter("tag", tagName.trim());
        }
        return q.getSingleResult();
    }

    /** 收藏数冗余计数增减（事务内调用，开发手册 5.3/9.2） */
    public int adjustFavoriteCount(EntityManager em, Long playlistId, int delta) {
        return em.createQuery("update Playlist p set p.favoriteCount = p.favoriteCount + :d where p.id = :id")
                .setParameter("d", delta)
                .setParameter("id", playlistId)
                .executeUpdate();
    }

    /** 详情只对上架歌单开放；下架详情不可访问（开发手册 5.1） */
    public Optional<Playlist> findOnlineById(EntityManager em, Long id) {
        try {
            return Optional.of(em.createQuery(
                            "select p from Playlist p left join fetch p.tags where p.id = :id and p.status = :status",
                            Playlist.class)
                    .setParameter("id", id)
                    .setParameter("status", ShelfStatus.ONLINE)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /** ===== 后台查询（开发手册 5.6）：关键字/状态筛选 + 分页 ===== */

    public List<Playlist> searchAdmin(EntityManager em, String keyword, ShelfStatus status, int page, int size) {
        StringBuilder jpql = new StringBuilder("select p from Playlist p");
        appendAdminWhere(jpql, keyword, status);
        return bindAdmin(em.createQuery(jpql + " order by p.id desc", Playlist.class), keyword, status)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .getResultList();
    }

    public long countAdmin(EntityManager em, String keyword, ShelfStatus status) {
        StringBuilder jpql = new StringBuilder("select count(p) from Playlist p");
        appendAdminWhere(jpql, keyword, status);
        return bindAdmin(em.createQuery(jpql.toString(), Long.class), keyword, status).getSingleResult();
    }

    private void appendAdminWhere(StringBuilder jpql, String keyword, ShelfStatus status) {
        List<String> conds = new ArrayList<>();
        if (status != null) {
            conds.add("p.status = :status");
        }
        if (keyword != null && !keyword.isBlank()) {
            conds.add("lower(p.title) like :kw");
        }
        if (!conds.isEmpty()) {
            jpql.append(" where ").append(String.join(" and ", conds));
        }
    }

    private <T> TypedQuery<T> bindAdmin(TypedQuery<T> q, String keyword, ShelfStatus status) {
        if (status != null) {
            q.setParameter("status", status);
        }
        if (keyword != null && !keyword.isBlank()) {
            q.setParameter("kw", "%" + keyword.trim().toLowerCase() + "%");
        }
        return q;
    }

    /** 批量统计曲目数，避免行内 N+1（开发手册 9.2） */
    public Map<Long, Long> trackCounts(EntityManager em, List<Long> playlistIds) {
        if (playlistIds.isEmpty()) {
            return Map.of();
        }
        List<Object[]> rows = em.createQuery(
                        "select t.playlist.id, count(t.id) from Track t where t.playlist.id in :ids group by t.playlist.id",
                        Object[].class)
                .setParameter("ids", playlistIds)
                .getResultList();
        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }

    /** 曲目整体替换前先清空旧行（事务内调用） */
    public int deleteTracksOf(EntityManager em, Long playlistId) {
        return em.createQuery("delete from Track t where t.playlist.id = :id")
                .setParameter("id", playlistId)
                .executeUpdate();
    }
}
