# 自然音乐歌单 · 接口文档

> 统一前缀 `/api`，响应结构 `{code, message, data}`；`code=0` 表示成功。
> 错误码：400 参数错误 / 401 未登录 / 403 无权限 / 404 资源不存在 / 409 冲突（重复收藏等）/ 500 系统错误。
> 未登录访问需登录接口返回 `code=401`；普通用户访问 `/api/admin/**` 返回 `code=403`。

## 1. 歌单（前台）

### GET /api/playlists?tag=&page=&size=
歌单列表，仅上架歌单，按标签筛选、服务端分页。

| 参数 | 说明 |
|---|---|
| tag | 可选，标签名精确匹配 |
| page | 页码，默认 1，正整数 |
| size | 每页条数，默认 10，最大 50 |

```json
{ "code": 0, "message": "ok", "data": {
  "list": [ { "id": 1, "title": "雨夜读书", "coverUrl": null, "tags": ["雨声","夜晚"], "favoriteCount": 12 } ],
  "total": 9, "page": 1, "size": 8 } }
```

### GET /api/playlists/{id}
歌单详情（含曲目列表）。歌单不存在或已下架返回 `code=404, message=歌单不存在或已下架`。

```json
{ "code": 0, "data": { "id": 1, "title": "雨夜读书", "description": "…", "coverUrl": null,
  "tags": ["雨声","夜晚"], "favoriteCount": 12,
  "tracks": [ { "trackNo": 1, "title": "屋檐滴雨", "durationSec": 1800 } ] } }
```

### GET /playlist?id={id}
歌单详情页（服务端渲染 HTML，SEO 可索引）。下架/不存在返回 HTTP 404 友好页；id 非法返回 HTTP 400。

## 2. 标签

### GET /api/tags
```json
{ "code": 0, "data": [ { "id": 1, "name": "雨声", "playlistCount": 2 } ] }
```

## 3. 用户与认证

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/auth/register | 注册并自动登录。用户名 3-20 位字母/数字/下划线；密码 6-64 位 |
| POST | /api/auth/login | 登录。失败返回 `code=401, message=用户名或密码错误`（不泄露账号是否存在） |
| POST | /api/auth/logout | 退出，会话失效 |
| GET | /api/auth/me | 当前登录用户 `{id, username, role}`；未登录 `code=401` |

登录态存容器会话（JSESSIONID，HttpOnly），有效期 7 天。角色：`USER` / `ADMIN`。

## 4. 收藏（全部需登录）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/favorites | body `{"playlistId":1}`；返回 `{playlistId, favoriteCount}`；重复收藏 `code=409` |
| DELETE | /api/favorites/{playlistId} | 取消收藏 |
| GET | /api/me/favorites?page=&size= | 我的收藏，按收藏时间倒序分页；下架歌单 `available=false` 保留记录 |

收藏归属一律取会话中的当前用户，不信任前端传入的 userId（防越权）。

## 5. 推荐占位

### GET /api/recommend
返回后台启用中的推荐位（按 sort_order）：`[{id, title, imageUrl, playlistId}]`；`playlistId` 可为 null（纯展示）。空配置返回空数组，前端降级隐藏。

## 6. 运营后台（/api/admin/**，仅 ADMIN，AdminAuthFilter 统一拦截）

### 歌单管理
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/admin/playlists?q=&status=&page=&size= | 列表：标题搜索、状态筛选（ONLINE/OFFLINE）、分页 |
| POST | /api/admin/playlists | 新增（默认下架）。body：`{title, description, coverUrl, tagIds:[], tracks:[{title,durationSec}]}` |
| GET | /api/admin/playlists/{id} | 编辑详情（含标签、曲目） |
| PUT 或 POST | /api/admin/playlists/{id} | 编辑保存（标签、曲目整体替换） |
| POST | /api/admin/playlists/{id}/status | 上下架。body `{"status":"ONLINE"|"OFFLINE"}`；上架要求至少一个标签 |

### 标签管理
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/admin/tags | 全部标签（含绑定歌单数） |
| POST | /api/admin/tags | 新增 `{name, sortOrder}`；名称唯一 |
| PUT 或 POST | /api/admin/tags/{id} | 编辑 |
| DELETE | /api/admin/tags/{id}?force=1 | 删除；有关联时返回 `409`，`force=1` 解除关联后删除 |

### 推荐位管理
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/admin/recommendations | 全部推荐位（含停用） |
| POST | /api/admin/recommendations | 新增 `{title, imageUrl, playlistId, sortOrder, enabled}` |
| PUT 或 POST | /api/admin/recommendations/{id} | 编辑（含启用/停用切换） |

### 封面上传
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/admin/upload | multipart 字段 `file`；仅 jpg/png/webp，≤2MB，校验文件头；返回 `{url:"/music-web/covers/{uuid}.jpg"}` |

### 封面读取
GET /covers/{uuid}.{ext} —— 服务端 UUID 文件名 + 正则白名单，防路径穿越；缓存 1 天。

## 7. SEO 相关

| 路径 | 说明 |
|---|---|
| /sitemap.xml | 动态生成：首页 + 全部在架歌单详情页 |
| /robots.txt | 静态：允许全站，Disallow /api/ 与 /admin.html |
