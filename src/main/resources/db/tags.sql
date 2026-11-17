USE music_web;

CREATE TABLE IF NOT EXISTS tags (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(50) NOT NULL COMMENT '标签名',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  CONSTRAINT uq_tag_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签';

CREATE TABLE IF NOT EXISTS playlist_tag (
  playlist_id BIGINT NOT NULL COMMENT '歌单ID',
  tag_id BIGINT NOT NULL COMMENT '标签ID',
  PRIMARY KEY (playlist_id, tag_id),
  KEY idx_pt_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单-标签';

INSERT IGNORE INTO tags (name, sort_order) VALUES
  ('雨声',1),('溪流',2),('鸟鸣',3),('森林',4),('海浪',5),('篝火',6),('白噪音',7),('夜晚',8);

SELECT id, name FROM tags ORDER BY sort_order;