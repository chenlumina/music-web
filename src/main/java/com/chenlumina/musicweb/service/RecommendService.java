package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.RecommendDao;
import com.chenlumina.musicweb.dto.RecommendDto;
import com.chenlumina.musicweb.entity.Recommendation;
import com.chenlumina.musicweb.util.DbUtil;

import java.util.ArrayList;
import java.util.List;

/** 推荐位业务：读取后台配置；空配置由前端降级展示（开发手册 5.4） */
public class RecommendService {

    private final RecommendDao recommendDao = new RecommendDao();

    public List<RecommendDto> listEnabled() {
        return DbUtil.withConnection(conn -> {
            List<RecommendDto> list = new ArrayList<>();
            for (Recommendation r : recommendDao.findEnabled(conn)) {
                list.add(new RecommendDto(r.getId(), r.getTitle(), r.getImageUrl(), r.getPlaylistId()));
            }
            return list;
        });
    }
}