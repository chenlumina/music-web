package com.chenlumina.musicweb.dao;

import com.chenlumina.musicweb.entity.Recommendation;
import jakarta.persistence.EntityManager;

import java.util.List;

/** 推荐位读取：仅启用的配置，按排序（开发手册 5.4，纯配置无算法） */
public class RecommendDao {

    public List<Recommendation> findEnabled(EntityManager em) {
        return em.createQuery(
                        "select r from Recommendation r where r.enabled = true order by r.sortOrder asc",
                        Recommendation.class)
                .getResultList();
    }

    /** 后台：全部推荐位（含停用），按排序（开发手册 5.6） */
    public List<Recommendation> findAll(EntityManager em) {
        return em.createQuery(
                        "select r from Recommendation r order by r.sortOrder asc, r.id asc",
                        Recommendation.class)
                .getResultList();
    }
}
