package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.PlaylistDao;
import com.chenlumina.musicweb.dao.TagDao;
import com.chenlumina.musicweb.dto.AdminTagDto;
import com.chenlumina.musicweb.dto.TagUpsertReq;
import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.entity.ShelfStatus;
import com.chenlumina.musicweb.entity.Tag;
import com.chenlumina.musicweb.util.ApiException;
import com.chenlumina.musicweb.util.DbUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 后台标签业务：增删改、排序；删除前校验绑定关系（开发手册 5.2/5.6） */
public class AdminTagService {

    private final TagDao tagDao = new TagDao();
    private final PlaylistDao playlistDao = new PlaylistDao();

    public List<AdminTagDto> listAll() {
        return DbUtil.withConnection(conn -> {
            Map<Long, Long> bound = tagDao.countBoundPlaylists(conn);
            List<AdminTagDto> list = new ArrayList<>();
            for (Tag t : tagDao.findAll(conn)) {
                list.add(new AdminTagDto(t.getId(), t.getName(), t.getSortOrder(),
                        bound.getOrDefault(t.getId(), 0L)));
            }
            return list;
        });
    }

    public Long create(TagUpsertReq req) {
        String name = cleanName(req);
        int sort = req.getSortOrder() == null ? 0 : req.getSortOrder();
        try {
            return DbUtil.inTransaction(conn -> {
                if (tagDao.findByName(conn, name).isPresent()) {
                    throw new ApiException(409, "标签名已存在");
                }
                Tag t = new Tag();
                t.setName(name);
                t.setSortOrder(sort);
                return tagDao.insert(conn, t);
            });
        } catch (ApiException e) {
            throw e;
        } catch (RuntimeException e) {
            // 并发重名的唯一约束兜底（开发手册 6.2）
            if (DbUtil.isDuplicateKey(e)) {
                throw new ApiException(409, "标签名已存在");
            }
            throw e;
        }
    }

    public void update(Long id, TagUpsertReq req) {
        String name = cleanName(req);
        int sort = req.getSortOrder() == null ? 0 : req.getSortOrder();
        try {
            DbUtil.inTransaction(conn -> {
                Tag t = tagDao.findById(conn, id);
                if (t == null) {
                    throw new ApiException(404, "标签不存在");
                }
                Optional<Tag> other = tagDao.findByName(conn, name);
                if (other.isPresent() && !other.get().getId().equals(id)) {
                    throw new ApiException(409, "标签名已存在");
                }
                t.setName(name);
                t.setSortOrder(sort);
                tagDao.update(conn, t);
                return null;
            });
        } catch (ApiException e) {
            throw e;
        } catch (RuntimeException e) {
            if (DbUtil.isDuplicateKey(e)) {
                throw new ApiException(409, "标签名已存在");
            }
            throw e;
        }
    }

    /** 删除前必须解除关联或明确强制（开发手册 5.2）；force=先解除全部关联再删除 */
    public void delete(Long id, boolean force) {
        DbUtil.inTransaction(conn -> {
            Tag t = tagDao.findById(conn, id);
            if (t == null) {
                throw new ApiException(404, "标签不存在");
            }
            List<Playlist> bound = tagDao.playlistsBoundTo(conn, id);
            if (!bound.isEmpty() && !force) {
                throw new ApiException(409,
                        "该标签已绑定 " + bound.size() + " 个歌单，请先解除关联或确认强制删除");
            }
            tagDao.unbindAll(conn, id);
            for (Playlist p : bound) {
                // 强制删除后若上架歌单失去最后一个标签，自动下架，保持"上架必须有标签"约束
                if (p.getStatus() == ShelfStatus.ONLINE
                        && tagDao.countTagsOfPlaylist(conn, p.getId()) == 0) {
                    playlistDao.updateStatus(conn, p.getId(), ShelfStatus.OFFLINE);
                }
            }
            tagDao.delete(conn, id);
            return null;
        });
    }

    private String cleanName(TagUpsertReq req) {
        String name = req.getName() == null ? "" : req.getName().trim();
        if (name.isEmpty()) {
            throw new ApiException(400, "标签名不能为空");
        }
        if (name.length() > 50) {
            throw new ApiException(400, "标签名不能超过 50 字");
        }
        if (req.getSortOrder() != null && req.getSortOrder() < 0) {
            throw new ApiException(400, "排序需为非负整数");
        }
        return name;
    }
}