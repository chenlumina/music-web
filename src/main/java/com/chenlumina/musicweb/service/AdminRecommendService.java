package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.PlaylistDao;
import com.chenlumina.musicweb.dao.RecommendDao;
import com.chenlumina.musicweb.dto.AdminRecommendDto;
import com.chenlumina.musicweb.dto.RecommendUpsertReq;
import com.chenlumina.musicweb.entity.Recommendation;
import com.chenlumina.musicweb.util.ApiException;
import com.chenlumina.musicweb.util.DbUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** 后台推荐位业务：纯配置维护，不做算法（开发手册 5.4/5.6） */
public class AdminRecommendService {

    private final RecommendDao recommendDao = new RecommendDao();
    private final PlaylistDao playlistDao = new PlaylistDao();

    public List<AdminRecommendDto> listAll() {
        return DbUtil.withConnection(conn -> {
            List<Recommendation> all = recommendDao.findAll(conn);
            List<Long> ids = all.stream()
                    .map(Recommendation::getPlaylistId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, String> titles = playlistDao.findTitles(conn, ids);
            List<AdminRecommendDto> rows = new ArrayList<>();
            for (Recommendation r : all) {
                rows.add(new AdminRecommendDto(r.getId(), r.getTitle(), r.getImageUrl(), r.getPlaylistId(),
                        r.getPlaylistId() == null ? null : titles.get(r.getPlaylistId()),
                        r.getSortOrder(), r.isEnabled()));
            }
            return rows;
        });
    }

    public Long create(RecommendUpsertReq req) {
        return DbUtil.inTransaction(conn -> {
            Recommendation r = new Recommendation();
            apply(conn, r, req);
            return recommendDao.insert(conn, r);
        });
    }

    public void update(Long id, RecommendUpsertReq req) {
        DbUtil.inTransaction(conn -> {
            Recommendation r = recommendDao.findById(conn, id);
            if (r == null) {
                throw new ApiException(404, "推荐位不存在");
            }
            apply(conn, r, req);
            recommendDao.update(conn, r);
            return null;
        });
    }

    private void apply(Connection conn, Recommendation r, RecommendUpsertReq req) throws SQLException {
        String title = req.getTitle() == null ? "" : req.getTitle().trim();
        if (title.isEmpty()) {
            throw new ApiException(400, "标题不能为空");
        }
        if (title.length() > 100) {
            throw new ApiException(400, "标题不能超过 100 字");
        }
        if (req.getImageUrl() != null && req.getImageUrl().trim().length() > 255) {
            throw new ApiException(400, "图片地址过长");
        }
        int sort = req.getSortOrder() == null ? 0 : req.getSortOrder();
        if (sort < 0) {
            throw new ApiException(400, "排序需为非负整数");
        }
        if (req.getPlaylistId() != null && !playlistDao.exists(conn, req.getPlaylistId())) {
            throw new ApiException(400, "跳转歌单不存在");
        }
        r.setTitle(title);
        r.setImageUrl(req.getImageUrl() == null || req.getImageUrl().isBlank()
                ? null : req.getImageUrl().trim());
        r.setPlaylistId(req.getPlaylistId());
        r.setSortOrder(sort);
        r.setEnabled(req.getEnabled() == null || req.getEnabled());
    }
}