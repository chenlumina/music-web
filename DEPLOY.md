# music-web 部署说明（DEPLOY.md）

本说明从零开始部署「自然音乐歌单」：JavaEE8（javax.servlet 4.0）+ JSP/JSTL + JDBC + MySQL 8，运行在 Tomcat 9 / JDK 11。
按顺序执行即可完成构建、初始化、启动与验证；页面与接口均为真实运行结果。

## 1. 环境前提

- JDK 11 或更高版本（本项目在 JDK 11.0.27 验证）
- Maven 3.8+（项目自带 `mvnw`，无需全局安装）
- Apache Tomcat **9.0.x**（Servlet 4.0）。**不能使用 Tomcat 10+**：Tomcat 10 改用 jakarta.servlet，本项目的 javax.servlet 代码无法部署
- MySQL 8.0（库名 `music_web`，字符集 utf8mb4）
- 端口：本机验证使用 Tomcat 9532、MySQL 3306；Docker Compose 使用宿主 9540；Tomcat 默认 8080，可在 `conf/server.xml` 调整

## 2.1 Docker Compose 部署（推荐）

仓库根目录已提供 `Dockerfile` 与 `docker-compose.yml`；Compose 同时启动 Tomcat 9 和 MySQL 8，MySQL 自动执行 `quick-deploy/init.sql`：

```bash
docker compose up -d --build
docker compose ps
curl http://localhost:9540/music-web/health
# 外部设备（同局域网）：http://<宿主机IP>:9540/music-web/
```

## 2.2 免构建部署（quick-deploy）

`quick-deploy/` 已提供预构建 WAR 与初始化脚本：

```bash
# 1) 初始化数据库（会重建 music_web 库与种子数据）
mysql -uroot -p < quick-deploy/init.sql

# 2) 部署 WAR
copy quick-deploy\music-web.war <CATALINA_BASE>\webapps\

# 3) 启动 Tomcat 9 后访问
#    http://localhost:9532/music-web/
#    演示账号 admin / admin123
```

> 预构建 WAR 内置 `root`/空密码连接。若本机 MySQL root 有密码，请按第 3、4 节从源码构建。

## 3. 从源码构建与部署

```bash
# 数据库
mysql -uroot -p < src/main/resources/db/init.sql

# 连接配置（开发环境）
#   src/main/resources/db.properties
#   db.url=...music_web?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8...
#   db.username=root
#   db.password=你的密码

# 构建（Windows）
.\mvnw.cmd package
# macOS/Linux: ./mvnw package
# 产物：target/music-web.war

# 部署
copy target\music-web.war <CATALINA_BASE>\webapps\
# 启动（非 IDE 示例）
<CATALINA_HOME>\bin\catalina.bat run
```

启动日志中应出现 `Deployment of web application archive [...]\music-web.war has finished`，
随后访问 `http://localhost:9532/music-web/` 返回首页。

## 4. 配置外置

- 数据库连接：`src/main/resources/db.properties`（打包后位于 `WEB-INF/classes`）。
  `DbUtil` 支持环境变量覆盖，便于容器/云环境：`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`；未设置时回退 `db.properties`。
- 封面上传目录：默认 `<catalina.base>/music-web-uploads/covers`；可通过 `db.properties` 的 `upload.dir` 指定可写目录。
- 生产环境不要把真实密码提交到仓库；外置配置或使用环境变量。

## 5. 部署后验证

```bash
# 一键断言（第 1 个参数为应用根地址）
bash verify-deploy.sh http://localhost:9532/music-web
```

脚本检查首页、登录页、歌单详情、列表 API、标签 API、sitemap 均返回 200，
并校验未登录访问 `/api/admin/tags` 返回 JSON `code=401`；全部通过输出 `DEPLOY OK`，任一失败退出码 1。

手工流程：登录 `admin/admin123` → 打开运营后台 → 编辑歌单 → 保存 → 首页/详情页可见；
在后台将歌单改为下架 → 前台首页不再展示、详情页返回 404 → 恢复上架后复现。

## 6. 常见故障与排查

| 现象 | 根因 | 处理 |
|---|---|---|
| 启动报 `BindException: Address already in use: bind` | 目标端口（示例 9532）已被其它 Tomcat 占用 | `netstat -ano | findstr :9532` 找到 PID，停止占用进程或修改 `conf/server.xml` 的 Connector port |
| 页面报 `ClassNotFoundException: javax.servlet.http.HttpServlet` 或 JSP 标签库不解析 | 误用 Tomcat 10+（jakarta.servlet） | 改用 Tomcat 9.0.x；本项目不得出现 jakarta.* |
| 启动日志无 Deployment 行、访问 404 | WAR 未放入 webapps 或上下文路径不符 | 确认文件名为 `music-web.war`，访问路径为 `/music-web/` |
| 登录/接口报数据库连接失败（Access denied / Communications link failure） | 账号密码、端口或 MySQL 未启动 | 核对 `db.properties`；`mysql -uroot -p` 可连通后重启应用 |
| 上传封面成功但访问 `/covers/...` 404 | 上传目录不可写或 `upload.dir` 指向不一致 | 检查 `<catalina.base>/music-web-uploads/covers` 权限；统一配置后重启 |
| 中文乱码 | 数据库/连接串字符集非 utf8mb4 或请求体未按 UTF-8 解析 | 建库使用 init.sql（utf8mb4）；JSON 请求头 `Content-Type: application/json; charset=utf-8` |
| 详情页 404 | 歌单本身为下架状态（业务设计如此） | 在后台恢复上架；下架歌单前台不可访问属于预期行为 |

## 7. 回滚

保留上一版本 `music-web.war`；回滚时替换 `webapps/music-web.war` 并重启 Tomcat。
数据库结构变更前先备份：`mysqldump -uroot music_web > backup.sql`。

## 8. 部署工件清单

| 工件 | 仓库路径 | 用途 |
|---|---|---|
| WAR 产物 | `target/music-web.war` | Maven 构建输出 |
| 免构建 WAR | `quick-deploy/music-web.war` | 直接部署（JDK 11/Tomcat 9） |
| 数据库初始化 | `src/main/resources/db/init.sql`、`quick-deploy/init.sql` | 建库建表 + 种子数据 |
| 连接配置 | `src/main/resources/db.properties` | 本地连接参数（可被环境变量覆盖） |
| 验证脚本 | `verify-deploy.sh` | 部署后一键断言 |
| 项目说明 | `README.md`、`docs/deploy.md` | 开发/部署细节 |