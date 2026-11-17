package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.PlaylistDao;
import com.chenlumina.musicweb.entity.Playlist;
import com.chenlumina.musicweb.util.ValidateException;

import java.util.List;
import java.util.Set;

/**
 * 歌单业务层：表单校验、业务规则都放这里，Servlet 只负责取参数和转发。
 * 规则沿用原始项目：标题必填且唯一、状态只允许 ONLINE/OFFLINE、前台只展示上架歌单。
 */
public class PlaylistService {

    private static final int TITLE_MAX = 100;
    private static final int DESC_MAX = 500;
    private static final Set<String> ALLOWED_STATUS = Set.of("ONLINE", "OFFLINE");

    private final PlaylistDao playlistDao = new PlaylistDao();

    public List<Playlist> listAll() {
        return playlistDao.findAll();
    }

    /**
     * 新增歌单。校验不通过时抛 ValidateException，由 Servlet 回显到表单。
     * 注意：校验失败必须在写库之前完成，不能先插入再判断。
     */
    public long create(String title, String description, String coverUrl, String status) {
        String safeTitle = title == null ? "" : title.trim();
        String safeDesc = description == null ? "" : description.trim();
        String safeStatus = (status == null || status.trim().isEmpty())
                ? "ONLINE" : status.trim().toUpperCase();
        checkBasic(safeTitle, safeDesc, safeStatus);

        if (playlistDao.countByTitle(safeTitle) > 0) {
            throw new ValidateException("歌单标题「" + safeTitle + "」已存在，请换一个");
        }

        String safeCover = coverUrl == null ? "" : coverUrl.trim();
        return playlistDao.insert(new Playlist(safeTitle, safeDesc, safeCover, safeStatus));
    }

    /**
     * 编辑更新。与新增共用基础校验，但标题查重排除自身（允许保留原标题）。
     * 真实业务规则：状态改为 OFFLINE 后前台立即不可见（见 listOnline）。
     */
    public int update(long id, String title, String description, String coverUrl, String status) {
        String safeTitle = title == null ? "" : title.trim();
        String safeDesc = description == null ? "" : description.trim();
        String safeStatus = (status == null || status.trim().isEmpty())
                ? "ONLINE" : status.trim().toUpperCase();
        checkBasic(safeTitle, safeDesc, safeStatus);

        if (playlistDao.countByTitleExcludeId(safeTitle, id) > 0) {
            throw new ValidateException("歌单标题「" + safeTitle + "」已存在，请换一个");
        }

        String safeCover = coverUrl == null ? "" : coverUrl.trim();
        Playlist playlist = new Playlist(safeTitle, safeDesc, safeCover, safeStatus);
        playlist.setId(id);
        return playlistDao.update(playlist);
    }

    public Playlist getById(long id) {
        return playlistDao.findById(id);
    }

    /** 前台只读列表：仅上架歌单可见 */
    public List<Playlist> listOnline() {
        return playlistDao.findOnline();
    }

    /** 新增/编辑共用的基础校验，消除两处重复 */
    private void checkBasic(String safeTitle, String safeDesc, String safeStatus) {
        if (safeTitle.isEmpty()) {
            throw new ValidateException("歌单标题不能为空");
        }
        if (safeTitle.length() > TITLE_MAX) {
            throw new ValidateException("歌单标题不能超过 " + TITLE_MAX + " 个字符");
        }
        if (safeDesc.length() > DESC_MAX) {
            throw new ValidateException("歌单描述不能超过 " + DESC_MAX + " 个字符");
        }
        if (!ALLOWED_STATUS.contains(safeStatus)) {
            throw new ValidateException("歌单状态只能是 ONLINE 或 OFFLINE");
        }
    }
}
