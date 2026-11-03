-- music_web 数据库初始化脚本 v1（2026-10-26）
-- 含 7 张表、唯一约束、索引与种子数据；可重复执行（重建全部表）
SET NAMES utf8mb4;
CREATE DATABASE IF NOT EXISTS music_web DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE music_web;

DROP TABLE IF EXISTS recommendations;
DROP TABLE IF EXISTS favorites;
DROP TABLE IF EXISTS playlist_tag;
DROP TABLE IF EXISTS tracks;
DROP TABLE IF EXISTS tags;
DROP TABLE IF EXISTS playlists;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'USER=注册用户 ADMIN=运营管理员',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uq_username UNIQUE (username)
) COMMENT='用户';

CREATE TABLE playlists (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  description TEXT,
  cover_url VARCHAR(255),
  status VARCHAR(20) NOT NULL DEFAULT 'ONLINE' COMMENT 'ONLINE=上架 OFFLINE=下架',
  favorite_count INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_status (status)
) COMMENT='歌单';

CREATE TABLE tracks (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  playlist_id BIGINT NOT NULL,
  track_no INT NOT NULL DEFAULT 1,
  title VARCHAR(100) NOT NULL,
  duration_sec INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_track_playlist FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE
) COMMENT='曲目';

CREATE TABLE tags (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT uq_tag_name UNIQUE (name)
) COMMENT='标签';

CREATE TABLE playlist_tag (
  playlist_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  PRIMARY KEY (playlist_id, tag_id),
  CONSTRAINT fk_pt_playlist FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE,
  CONSTRAINT fk_pt_tag FOREIGN KEY (tag_id) REFERENCES tags(id)
) COMMENT='歌单-标签关联';

CREATE TABLE favorites (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  playlist_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_fav_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_fav_playlist FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE CASCADE,
  CONSTRAINT uq_user_playlist UNIQUE (user_id, playlist_id)
) COMMENT='收藏';
CREATE INDEX idx_fav_user ON favorites(user_id);

CREATE TABLE recommendations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL,
  image_url VARCHAR(255),
  playlist_id BIGINT,
  sort_order INT NOT NULL DEFAULT 0,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_rec_playlist FOREIGN KEY (playlist_id) REFERENCES playlists(id) ON DELETE SET NULL
) COMMENT='推荐位';

-- ===== 种子数据 =====
INSERT INTO users (username, password_hash, role) VALUES
('admin', '$2b$10$vtC9psVVFWpv73VXY2jS2eRo8JgKK4qBeSND1j7TdasnIrlE6Qy12', 'ADMIN'),
('test01', '$2b$10$OWouvFrDVDEvgY9oa0a8MOJmEX6n9V1k0IcE5f7yQ3VILqJifJDAy', 'USER');

INSERT INTO tags (name, sort_order) VALUES
('雨声',1),('溪流',2),('鸟鸣',3),('森林',4),('海浪',5),('篝火',6),('白噪音',7),('夜晚',8);

INSERT INTO playlists (title, description, status, favorite_count) VALUES
('雨夜读书','淅淅沥沥的雨声伴你翻过每一页','ONLINE',12),
('山间溪流','清冽的溪水从卵石上流过','ONLINE',8),
('清晨鸟鸣','林间的第一缕鸟叫，唤醒清醒的一天','ONLINE',15),
('深林漫步','参天古木间的风声与叶响','ONLINE',5),
('海浪拍岸','潮起潮落，思绪随浪放空','ONLINE',20),
('篝火之夜','木柴噼啪作响的温暖夜晚','ONLINE',9),
('午后细雨','适合小憩的温柔雨声','ONLINE',3),
('星空营地','旷野、帐篷与远处的虫鸣','ONLINE',7),
('雷雨实验','暴雨与雷鸣的白噪音采样（内部测试）','OFFLINE',0);

INSERT INTO tracks (playlist_id, track_no, title, duration_sec) VALUES
(1,1,'屋檐滴雨',1800),(1,2,'雨打芭蕉',2400),(1,3,'远雷阵阵',2000),
(2,1,'浅滩流水',1800),(2,2,'溪谷回声',2100),
(3,1,'黎明第一声',900),(3,2,'林间合唱',1500),(3,3,'枝头对话',1200),
(4,1,'针叶林风',2400),(4,2,'落叶小径',1800),
(5,1,'平缓浪潮',2700),(5,2,'礁石浪花',1500),(5,3,'退潮时分',2100),
(6,1,'火星噼啪',1200),(6,2,'柴堆低语',1800),
(7,1,'细雨绵绵',3600),
(8,1,'营灯摇曳',1500),(8,2,'虫鸣四野',2400),
(9,1,'雷暴采样',900);

INSERT INTO playlist_tag (playlist_id, tag_id) VALUES
(1,1),(1,8),(2,2),(2,4),(3,3),(3,4),(4,4),(5,5),(5,7),(6,6),(6,8),(7,1),(7,7),(8,6),(8,8),(9,1),(9,7);

INSERT INTO recommendations (title, image_url, playlist_id, sort_order, enabled) VALUES
('今日主打·海浪拍岸', NULL, 5, 1, 1),
('助眠精选·雨夜读书', NULL, 1, 2, 1),
('即将上线·白噪音专栏', NULL, NULL, 3, 0);
