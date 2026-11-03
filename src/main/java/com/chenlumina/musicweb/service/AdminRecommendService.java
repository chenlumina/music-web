package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.RecommendDao;
import com.chenlumina.musicweb.dto.AdminRecommendDto;
import com.chenlumina.musicweb.dto.RecommendUpsertReq;
import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.entity.Recommendation;
import com.chenlumina.musicweb.util.ApiException;
import com.chenlumina.musicweb.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 后台推荐位业务：纯配置维护，不做算法（开发手册 5.4/5.6） */
public class AdminRecommendService {

    private final RecommendDao recommendDao = new RecommendDao();

    public List<AdminRecommendDto> listAll() {
        try (EntityManager em = JpaUtil.em()) {
            List<Recommendation> all = recommendDao.findAll(em);
            List<Long> ids = all.stream()
                    .map(Recommendation::getPlaylistId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            Map<Long, String> titles = new HashMap<>();
            if (!ids.isEmpty()) {
                em.createQuery("select p.id, p.title from Playlist p where p.id in :ids", Object[].class)
                        .setParameter("ids", ids)
                        .getResultList()
                        .forEach(row -> titles.put((Long) row[0], (String) row[1]));
            }
            return all.stream()
                    .map(r -> new AdminRecommendDto(r.getId(), r.getTitle(), r.getImageUrl(), r.getPlaylistId(),
                            r.getPlaylistId() == null ? null : titles.get(r.getPlaylistId()),
                            r.getSortOrder(), r.isEnabled()))
                    .toList();
        }
    }

    public Long create(RecommendUpsertReq req) {
        try (EntityManager em = JpaUtil.em()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Recommendation r = new Recommendation();
                apply(r, req, em);
                em.persist(r);
                tx.commit();
                return r.getId();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
        }
    }

    public void update(Long id, RecommendUpsertReq req) {
        try (EntityManager em = JpaUtil.em()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Recommendation r = em.find(Recommendation.class, id);
                if (r == null) {
                    throw new ApiException(404, "推荐位不存在");
                }
                apply(r, req, em);
                tx.commit();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
        }
    }

    private void apply(Recommendation r, RecommendUpsertReq req, EntityManager em) {
        String title = req.title() == null ? "" : req.title().trim();
        if (title.isEmpty()) {
            throw new ApiException(400, "标题不能为空");
        }
        if (title.length() > 100) {
            throw new ApiException(400, "标题不能超过 100 字");
        }
        if (req.imageUrl() != null && req.imageUrl().trim().length() > 255) {
            throw new ApiException(400, "图片地址过长");
        }
        int sort = req.sortOrder() == null ? 0 : req.sortOrder();
        if (sort < 0) {
            throw new ApiException(400, "排序需为非负整数");
        }
        if (req.playlistId() != null && em.find(Playlist.class, req.playlistId()) == null) {
            throw new ApiException(400, "跳转歌单不存在");
        }
        r.setTitle(title);
        r.setImageUrl(req.imageUrl() == null || req.imageUrl().isBlank() ? null : req.imageUrl().trim());
        r.setPlaylistId(req.playlistId());
        r.setSortOrder(sort);
        r.setEnabled(req.enabled() == null || req.enabled());
    }
}
