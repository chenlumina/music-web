package com.chenlumina.musicweb.service;

import com.chenlumina.musicweb.dao.TrackDao;
import com.chenlumina.musicweb.entity.Track;
import com.chenlumina.musicweb.util.ValidateException;

import java.util.List;

/** 曲目业务：标题必填、时长非负 */
public class TrackService {

    private final TrackDao trackDao = new TrackDao();

    public List<Track> list(long playlistId) {
        return trackDao.findByPlaylist(playlistId);
    }

    public long add(long playlistId, String title, String artist, int durationSec) {
        String safeTitle = title == null ? "" : title.trim();
        if (safeTitle.isEmpty()) {
            throw new ValidateException("曲目标题不能为空");
        }
        if (safeTitle.length() > 200) {
            throw new ValidateException("曲目标题不能超过 200 个字符");
        }
        if (durationSec < 0) {
            throw new ValidateException("时长不能为负数");
        }
        String safeArtist = artist == null ? "" : artist.trim();
        if (safeArtist.length() > 100) {
            throw new ValidateException("艺术家名称不能超过 100 个字符");
        }
        Track track = new Track();
        track.setPlaylistId(playlistId);
        track.setTitle(safeTitle);
        track.setArtist(safeArtist);
        track.setDurationSec(durationSec);
        return trackDao.insert(track);
    }

    public void remove(long id) {
        trackDao.delete(id);
    }
}