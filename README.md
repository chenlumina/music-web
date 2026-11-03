# 自然音乐歌单（music-web）

Java EE 课程实践项目：一个以雨声、溪流、鸟鸣、篝火等自然声音为主题的歌单网站，包含前台浏览/收藏与运营后台。

## 功能特性

### 前台
- 首页：运营推荐位 coverflow、标签筛选、歌单分页、3D 翻转卡片、骨架屏与 toast
- 歌单详情：服务端渲染（利于 SEO），展示标签、描述与曲目单
- 用户注册 / 登录 / 退出，密码使用 BCrypt 存储，登录态存容器会话
- 收藏 / 取消收藏、我的收藏（下架歌单保留记录并标记不可用）
- SEO：`/sitemap.xml`、`/robots.txt`、详情页 JSON-LD 结构化数据

### 后台（仅 ADMIN）
- 歌单管理：新增、编辑、上下架、曲目整体替换、封面上传
- 标签管理：新增、编辑、删除（有关联时需二次确认强制解除）
- 推荐位管理：排序、启用/停用、关联跳转歌单
- `AdminAuthFilter` 统一拦截 `/api/admin/**`

## 技术栈

| 层次 | 技术 |
|---|---|
| Web | Jakarta EE 10 / Servlet 6.0、JSP + JSTL |
| 持久层 | Hibernate ORM 6.6（JPA）、MySQL 8.0 |
| JSON | Jackson（含 JSR-310 时间模块） |
| 安全 | jBCrypt 密码摘要、Session 登录态 |
| 构建 | Maven（WAR 包） |
| 运行 | Tomcat 10.1、JDK 17+ |

## 快速部署（免构建）

仓库根目录的 [quick-deploy/](quick-deploy/) 已提供可直接部署的产物：

| 文件 | 说明 |
|---|---|
| [`music-web.war`](quick-deploy/music-web.war) | 预构建 WAR 包（JDK 17+ / Tomcat 10.1） |
| [`init.sql`](quick-deploy/init.sql) | 数据库初始化脚本 |

三步启动：导入 `init.sql` → 把 WAR 放进 Tomcat 的 `webapps/` → 访问 `http://localhost:8080/music-web/`（账号 `admin / admin123`）。
预构建 WAR 内置数据库配置为 `root` / 空密码；如果密码不同，请修改 `db.properties` 后重新构建，详见 [quick-deploy/README.md](quick-deploy/README.md)。

## 快速开始

### 1. 环境要求

- JDK 17 或更高版本
- MySQL 8.0
- Tomcat 10.1（Servlet 6.0 容器）

### 2. 初始化数据库

```bash
mysql -uroot -p < src/main/resources/db/init.sql
```

脚本会创建 `music_web` 库、7 张表，并写入种子数据（歌单、曲目、标签、推荐位、演示账号）。
注意：脚本包含 `DROP TABLE`，重复执行会**清空并重建**全部表。

### 3. 配置数据库连接

编辑 `src/main/resources/db.properties`：

```properties
db.url=jdbc:mysql://localhost:3306/music_web?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true&useUnicode=true
db.username=root
db.password=你的密码
db.showSql=false
# 可选：封面上传目录，默认 <catalina.base>/music-web-uploads/covers
# upload.dir=D:/music-web-uploads
```

### 4. 构建

```bash
# Windows
.\mvnw.cmd package

# macOS / Linux
./mvnw package
```

产物：`target/music-web.war`。

### 5. 部署

将 `music-web.war` 复制到 Tomcat 的 `webapps/` 目录，启动 Tomcat 后访问：

```
http://localhost:8080/music-web/
```

> 若使用 IDEA 的 exploded 部署，context 通常为 `/music_web_war_exploded`，前端已做 context 路径自适应。

### 演示账号

| 账号 | 密码 | 角色 |
|---|---|---|
| `admin` | `admin123` | 管理员（可进入运营后台） |

普通用户可在登录页切换到“注册”自行创建。

## 目录结构

```
music-web/
├── src/main/java/com/chenlumina/musicweb/
│   ├── dao/          # JPA 查询与写入
│   ├── dto/          # 请求/响应数据结构
│   ├── entity/       # JPA 实体（7 张表）
│   ├── service/      # 业务与事务边界
│   ├── util/         # JSON、JPA、上传、异常工具
│   └── web/          # Servlet 与 AdminAuthFilter
├── src/main/resources/
│   ├── db/init.sql   # 建库建表 + 种子数据
│   ├── db.properties # 数据库与上传目录配置
│   └── META-INF/persistence.xml
├── src/main/webapp/
│   ├── index.jsp / playlist.jsp / error.jsp
│   ├── login.html / my-favorites.html / admin.html
│   ├── assets/       # 公共 JS、主题 CSS、内置图片
│   └── robots.txt
├── quick-deploy/     # 免构建部署包（WAR + init.sql）
├── docs/             # 接口、部署、测试报告
└── pom.xml
```

## 接口文档

统一前缀 `/api`，响应结构：

```json
{ "code": 0, "message": "ok", "data": {} }
```

`code=0` 表示成功；错误码：400 参数错误 / 401 未登录 / 403 无权限 / 404 资源不存在 / 409 冲突 / 500 系统错误。

完整接口见 [docs/api.md](docs/api.md)。

## 配置与运维

- 数据库连接、上传目录均在 `src/main/resources/db.properties` 中配置；生产环境应将配置外置，禁止提交真实密钥。
- 封面仅支持 jpg / jpeg / png / webp，单文件不超过 2MB，服务端会校验文件头。
- 上传文件按 UUID 重命名，读取接口使用文件名白名单，避免路径穿越。
- 部署与上线检查清单见 [docs/deploy.md](docs/deploy.md)。
- 联调验收记录见 [docs/test-report.md](docs/test-report.md)。

## 已知限制

- 曲目播放为占位，当前仅展示曲目信息。
- 推荐位为运营人工配置，暂未实现推荐算法。
- 未实现 CSRF Token、推荐位曝光/点击埋点与更细粒度操作日志。
- 后台推荐位关联歌单下拉、首页收藏状态、我的收藏列表均按前 50 条处理，数据量较大时需补充分页。
