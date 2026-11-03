package com.chenlumina.musicweb.dao;

import com.chenlumina.musicweb.entity.Favorite;
import jakarta.persistence.EntityManager;

import java.util.List;

/** 收藏查询与写入；user_id+playlist_id 唯一约束由数据库兜底（开发手册 5.3） */
public class FavoriteDao {

    public boolean exists(EntityManager em, Long userId, Long playlistId) {
        return em.createQuery("select count(f) from Favorite f where f.userId = :u and f.playlistId = :p", Long.class)
                .setParameter("u", userId)
                .setParameter("p", playlistId)
                .getSingleResult() > 0;
    }

    public void insert(EntityManager em, Favorite favorite) {
        em.persist(favorite);
    }

    /** @return 删除行数（0 = 本来就没收藏） */
    public int delete(EntityManager em, Long userId, Long playlistId) {
        return em.createQuery("delete from Favorite f where f.userId = :u and f.playlistId = :p")
                .setParameter("u", userId)
                .setParameter("p", playlistId)
                .executeUpdate();
    }

    /** 我的收藏按收藏时间倒序、分页（开发手册 5.3） */
    public List<Favorite> pageByUser(EntityManager em, Long userId, int page, int size) {
        return em.createQuery("select f from Favorite f where f.userId = :u order by f.createdAt desc, f.id desc", Favorite.class)
                .setParameter("u", userId)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .getResultList();
    }

    public long countByUser(EntityManager em, Long userId) {
        return em.createQuery("select count(f) from Favorite f where f.userId = :u", Long.class)
                .setParameter("u", userId)
                .getSingleResult();
    }
}
