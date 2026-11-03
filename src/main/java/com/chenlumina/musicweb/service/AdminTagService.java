package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.TagDao;
import com.chenlumina.musicweb.dto.AdminTagDto;
import com.chenlumina.musicweb.dto.TagUpsertReq;
import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.entity.ShelfStatus;
import com.chenlumina.musicweb.entity.Tag;
import com.chenlumina.musicweb.util.ApiException;
import com.chenlumina.musicweb.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.hibernate.exception.ConstraintViolationException;

import java.util.List;
import java.util.Map;

/** 后台标签业务：增删改、排序；删除前校验绑定关系（开发手册 5.2/5.6） */
public class AdminTagService {

    private final TagDao tagDao = new TagDao();

    public List<AdminTagDto> listAll() {
        try (EntityManager em = JpaUtil.em()) {
            Map<Long, Long> bound = tagDao.countBoundPlaylists(em);
            return tagDao.findAll(em).stream()
                    .map(t -> new AdminTagDto(t.getId(), t.getName(), t.getSortOrder(),
                            bound.getOrDefault(t.getId(), 0L)))
                    .toList();
        }
    }

    public Long create(TagUpsertReq req) {
        String name = cleanName(req);
        int sort = req.sortOrder() == null ? 0 : req.sortOrder();
        try (EntityManager em = JpaUtil.em()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                if (tagDao.findByName(em, name).isPresent()) {
                    throw new ApiException(409, "标签名已存在");
                }
                Tag t = new Tag();
                t.setName(name);
                t.setSortOrder(sort);
                em.persist(t);
                tx.commit();
                return t.getId();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                if (e instanceof ApiException api) throw api;
                // 并发重名的唯一约束兜底（开发手册 6.2）
                if (e instanceof ConstraintViolationException) {
                    throw new ApiException(409, "标签名已存在");
                }
                throw e;
            }
        }
    }

    public void update(Long id, TagUpsertReq req) {
        String name = cleanName(req);
        int sort = req.sortOrder() == null ? 0 : req.sortOrder();
        try (EntityManager em = JpaUtil.em()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Tag t = em.find(Tag.class, id);
                if (t == null) {
                    throw new ApiException(404, "标签不存在");
                }
                tagDao.findByName(em, name)
                        .filter(other -> !other.getId().equals(id))
                        .ifPresent(other -> { throw new ApiException(409, "标签名已存在"); });
                t.setName(name);
                t.setSortOrder(sort);
                tx.commit();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                if (e instanceof ApiException api) throw api;
                if (e instanceof ConstraintViolationException) {
                    throw new ApiException(409, "标签名已存在");
                }
                throw e;
            }
        }
    }

    /** 删除前必须解除关联或明确强制（开发手册 5.2）；force=先解除全部关联再删除 */
    public void delete(Long id, boolean force) {
        try (EntityManager em = JpaUtil.em()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Tag t = em.find(Tag.class, id);
                if (t == null) {
                    throw new ApiException(404, "标签不存在");
                }
                List<Playlist> bound = tagDao.playlistsBoundTo(em, id);
                if (!bound.isEmpty() && !force) {
                    throw new ApiException(409, "该标签已绑定 " + bound.size() + " 个歌单，请先解除关联或确认强制删除");
                }
                for (Playlist p : bound) {
                    p.getTags().remove(t);
                    // 强制删除后若上架歌单失去最后一个标签，自动下架，保持"上架必须有标签"约束
                    if (p.getStatus() == ShelfStatus.ONLINE && p.getTags().isEmpty()) {
                        p.setStatus(ShelfStatus.OFFLINE);
                    }
                }
                em.remove(t);
                tx.commit();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
        }
    }

    private String cleanName(TagUpsertReq req) {
        String name = req.name() == null ? "" : req.name().trim();
        if (name.isEmpty()) {
            throw new ApiException(400, "标签名不能为空");
        }
        if (name.length() > 50) {
            throw new ApiException(400, "标签名不能超过 50 字");
        }
        if (req.sortOrder() != null && req.sortOrder() < 0) {
            throw new ApiException(400, "排序需为非负整数");
        }
        return name;
    }
}
