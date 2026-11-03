package com.chenlumina.musicweb.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tracks")
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    @Column(name = "track_no", nullable = false)
    private int trackNo;

    @Column(nullable = false, length = 100)
    private String title;

    /** 时长（秒） */
    @Column(name = "duration_sec", nullable = false)
    private int durationSec;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Playlist getPlaylist() { return playlist; }
    public void setPlaylist(Playlist playlist) { this.playlist = playlist; }
    public int getTrackNo() { return trackNo; }
    public void setTrackNo(int trackNo) { this.trackNo = trackNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getDurationSec() { return durationSec; }
    public void setDurationSec(int durationSec) { this.durationSec = durationSec; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
