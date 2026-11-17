-- music-web 数据库初始化脚本
-- 对应实验二：歌单列表查询 + 新增
SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS music_web DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE music_web;

DROP TABLE IF EXISTS playlists;

CREATE TABLE playlists (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  title VARCHAR(100) NOT NULL COMMENT '歌单标题，业务上唯一',
  description VARCHAR(500) NOT NULL DEFAULT '' COMMENT '歌单描述',
  cover_url VARCHAR(255) NOT NULL DEFAULT '' COMMENT '封面文件名',
  status VARCHAR(20) NOT NULL DEFAULT 'ONLINE' COMMENT 'ONLINE=上架 OFFLINE=下架',
  favorite_count INT NOT NULL DEFAULT 0 COMMENT '收藏数',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  CONSTRAINT uq_playlist_title UNIQUE (title),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单';

-- 测试数据（至少 2 组）
INSERT INTO playlists (title, description, cover_url, status) VALUES
  ('雨夜书房', '窗外下雨，室内只留一盏灯。适合写作与深度阅读。', 'cover-rain.jpg', 'ONLINE'),
  ('溪边清晨', '溪水、鸟鸣与薄雾，一天里最清醒的四十分钟。', 'cover-stream.jpg', 'ONLINE'),
  ('篝火与夜谈', '木柴的爆裂声，适合睡前放空。', 'cover-campfire.jpg', 'ONLINE'),
  ('海浪白噪音', '连续的海浪拍岸声，用于午休与冥想。', 'cover-waves.jpg', 'OFFLINE');

SELECT id, title, status, created_at FROM playlists ORDER BY id;

CREATE TABLE IF NOT EXISTS favorites (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  playlist_id BIGINT NOT NULL COMMENT '歌单ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  CONSTRAINT uq_fav_user_playlist UNIQUE (user_id, playlist_id),
  KEY idx_fav_playlist (playlist_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏';
