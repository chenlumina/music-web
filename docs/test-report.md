# 测试报告 · 全站功能与 Java EE 8 迁移回归

- 日期：2026-11-24
- 被测版本：完整前台 + 运营后台（Java EE 8 / JDBC 版，构建 `target/music-web.war`）
- 测试环境：Windows 10 + Tomcat 9.0.117（本机实例，端口 9532，上下文 `/music-web`）+ JDK 11 + MySQL 8.0.46（music_web 库，种子数据）
- 方式：接口断言（HTTP/JSON，90 项，9532 端口复跑 90/90）+ 浏览器实测（Puppeteer 桌面 1440×900 与 430px 移动视口；截图采集于 8080 的同一构建）

## 必测用例（开发手册 10.2）

| # | 用例 | 结果 | 证据 |
|---|---|---|---|
| 1 | 标签筛选结果正确 | 通过 | `GET /api/playlists?tag=雨声` → total=2（午后细雨/雨夜读书），均含该标签 |
| 2 | 未登录收藏跳登录并回跳 | 通过 | 首页未登录点收藏 → 跳 `login.html?redirect=/music-web/`；登录后回跳首页并收藏成功（toast「已加入收藏」） |
| 3 | 重复收藏不产生重复记录 | 通过 | 首次收藏 `code=0`；重复 POST 返回 `code=409 该歌单已在收藏中`，favorites 表唯一约束兜底 |
| 4 | 取消收藏后我的收藏同步移除 | 通过 | 收藏后列表出现《雨夜读书》；取消后该条移除，列表继续展示其他已收藏歌单 |
| 5 | 下架歌单前台不可访问 | 通过 | API `GET /api/playlists/9` → `code=404`；详情页 `GET /playlist?id=9` → HTTP 404 友好页 |
| 6 | 推荐位为空/跳转失效不破坏首页 | 通过 | 有跳转的推荐位点击打开歌单弹窗；未关联的提示「这个推荐位未关联歌单」；推荐接口失败时前端静默隐藏区块 |
| 7 | 普通用户无法进入后台 | 通过 | USER 账号访问 `/api/admin/tags` → `code=403 无权限访问后台`；admin.html 对非管理员显示拦截页 |
| 8 | 分页、空态、错误态、移动端适配 | 通过 | `page=2&size=4` 返回正确页数据；空结果展示空态引导；404/400 友好错误页；430px 视口实测 hero/卡片/详情页布局正常 |

## 功能验证

| 项 | 结果 | 说明 |
|---|---|---|
| 服务端渲染详情页 `/playlist?id=` | 通过 | 标题/描述/标签/曲目单完整渲染；收藏按钮状态同步；id 非法 HTTP 400 |
| 详情页 404 语义 | 通过 | 下架(id=9)、不存在(id=999) 均 HTTP 404；响应体为品牌友好错误页 |
| sitemap.xml | 通过 | HTTP 200 application/xml；含首页 + 在架歌单详情页；下架歌单未列入 |
| robots.txt | 通过 | Allow 全站；Disallow /api/、/admin.html |
| 结构化数据 | 通过 | 详情页输出 schema.org MusicPlaylist JSON-LD（`<` 已转义防脚本逃逸） |
| XSS 防护 | 通过 | JSP 输出全部经 `c:out` 转义；测试数据含中文/标点正常显示 |
| 后台歌单/标签/推荐位/上传 | 通过 | 新增歌单默认下架、上架需标签、曲目整体替换、标签强制删除联动下架、封面上传与伪造文件拦截均验证通过 |
| 课程实验附加页 | 通过 | `/hello` 返回参数与服务器时间；`/health` 200 `OK db=up`；`/lab/playlists` 未登录 302 → login.html，登录后 JSP 列表/新增正常；`verify-deploy.sh` 输出 `DEPLOY OK` |

## 回归与修复记录

- 详情页视图模型统一用 Map 传递，JSP/EL 不依赖 record 访问器。
- 修复：EL 浮点除法导致时长显示 `30.0:00` → Servlet 端格式化为 `m:ss`。
- 修复：品牌名在窄视口换行 → `.brand` 增加 `white-space: nowrap`。
- 本轮 Java EE 8 迁移：`jakarta.servlet` → `javax.servlet`，Hibernate/JPA → JDBC（PreparedStatement + 手动事务）；
  接口路径、JSON 字段、错误码与页面结构保持不变。迁移后接口断言 90 项全部通过，桌面与移动端页面渲染正常。

## 遗留事项（P1）

- CSRF Token、推荐位曝光/点击埋点、操作日志增强（手册 5.6/9.1，超出 MVP 范围）。
- 歌单数超过 50 后 sitemap 已按分页循环覆盖，暂无需 sitemap index。
- 浏览器默认请求 `/favicon.ico` 会返回 404（原版即未配置站点图标），不影响页面与接口。