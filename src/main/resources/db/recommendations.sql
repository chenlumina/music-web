USE music_web;

CREATE TABLE IF NOT EXISTS recommendations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  playlist_id BIGINT NOT NULL COMMENT '歌单ID',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  CONSTRAINT uq_reco_playlist UNIQUE (playlist_id),
  KEY idx_reco_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐';