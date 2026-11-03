package com.chenlumina.musicweb.dao;

import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.entity.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 标签查询：全部标签 + 各标签在架歌单数（开发手册 5.2） */
public class TagDao {

    public List<Tag> findAll(EntityManager em) {
        return em.createQuery("select t from Tag t order by t.sortOrder asc", Tag.class)
                .getResultList();
    }

    /** 标签 -> 在架歌单数；无歌单的标签计数为 0，也要出现在标签页 */
    public Map<Long, Long> countOnlinePlaylists(EntityManager em) {
        List<Object[]> rows = em.createQuery(
                        "select t.id, count(p.id) from Playlist p join p.tags t "
                                + "where p.status = com.chenlumina.musicweb.entity.ShelfStatus.ONLINE "
                                + "group by t.id", Object[].class)
                .getResultList();
        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }

    /** ===== 后台查询（开发手册 5.6） ===== */

    public Optional<Tag> findByName(EntityManager em, String name) {
        try {
            return Optional.of(em.createQuery("select t from Tag t where t.name = :name", Tag.class)
                    .setParameter("name", name)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /** 各标签绑定歌单数（含下架，运营视角；无绑定的标签计数 0） */
    public Map<Long, Long> countBoundPlaylists(EntityManager em) {
        List<Object[]> rows = em.createQuery(
                        "select t.id, count(p.id) from Playlist p join p.tags t group by t.id", Object[].class)
                .getResultList();
        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }

    public List<Playlist> playlistsBoundTo(EntityManager em, Long tagId) {
        return em.createQuery("select p from Playlist p join p.tags t where t.id = :id", Playlist.class)
                .setParameter("id", tagId)
                .getResultList();
    }
}
