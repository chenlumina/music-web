package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.RecommendDao;
import com.chenlumina.musicweb.dto.RecommendDto;
import com.chenlumina.musicweb.util.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

/** 推荐位业务：读取后台配置；空配置由前端降级展示（开发手册 5.4） */
public class RecommendService {

    private final RecommendDao recommendDao = new RecommendDao();

    public List<RecommendDto> listEnabled() {
        try (EntityManager em = JpaUtil.em()) {
            return recommendDao.findEnabled(em).stream()
                    .map(r -> new RecommendDto(r.getId(), r.getTitle(), r.getImageUrl(), r.getPlaylistId()))
                    .toList();
        }
    }
}
