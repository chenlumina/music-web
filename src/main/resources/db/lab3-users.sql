-- 实验三：用户表与种子账号（沿用 music-web 的 users 结构与 BCrypt 存储规则）
SET NAMES utf8mb4;
USE music_lab;

DROP TABLE IF EXISTS users;

CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  username VARCHAR(50) NOT NULL COMMENT '登录名，3-20 位字母/数字/下划线',
  password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt 摘要，禁止存明文',
  role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'USER=注册用户 ADMIN=运营管理员',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  CONSTRAINT uq_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- 种子账号：admin/admin123（ADMIN，摘要直接沿用 music-web init.sql 的 $2b$ 值，
--           服务端 normalizeHash 把 $2b$ 归一为 jBCrypt 认识的 $2a$）
--           test01/test123（USER，摘要由 jBCrypt 0.4 生成，$2a$ 前缀）
INSERT INTO users (username, password_hash, role) VALUES
  ('admin', '$2b$10$vtC9psVVFWpv73VXY2jS2eRo8JgKK4qBeSND1j7TdasnIrlE6Qy12', 'ADMIN'),
  ('test01', '$2a$10$sEBGUxPqIKl3.ALI63P6z.ryvfv5UzP1GbCJ2LansJ/puNNlz5o1e', 'USER');

SELECT id, username, role, created_at FROM users ORDER BY id;