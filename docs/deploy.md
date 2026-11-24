# 部署与运维说明

## 1. 构建

```powershell
# 需要 JDK 11+；项目自带 Maven Wrapper，无需全局安装 Maven
.\mvnw.cmd package -DskipTests
# 产出 target\music-web.war
```

> 构建与运行必须使用 Servlet 4.0 基线：javax.servlet + Tomcat 9。Tomcat 10+ 使用 jakarta.servlet，不能部署本 war。

## 2. 部署到 Tomcat 9

1. 将 `music-web.war` 复制到 Tomcat 的 `webapps/` 目录，启动即自动解压部署。
2. 访问 `http://<host>:<port>/music-web/`。
3. Tomcat 默认 HTTP 端口为 8080；本项目本机验证使用 9532，可在 `conf/server.xml` 中调整。

> IDEA exploded 部署（开发用）：本机实例通过 `conf/Catalina/localhost/music-web.xml` 挂到 `/music-web`；若使用 IDEA 默认 exploded context（常见 `/music_web_war_exploded`），前端 JS 会自动识别 context 前缀。

## 3. 配置外置（开发手册 11 章）

- 数据库连接：`src/main/resources/db.properties`（随 war 打进 `WEB-INF/classes`）。
  生产环境如需外置，将同名文件放到应用服务器 classpath 优先目录，或修改后重新打包；**禁止将生产密钥提交仓库**。
- 封面上传目录：默认 `<catalina.base>/music-web-uploads/covers`，可在 db.properties 中用 `upload.dir` 指定其他挂载目录。
- 上传目录需随备份策略一起备份。

## 4. 数据库

- MySQL 8，库名 `music_web`，字符集 utf8mb4；初始化脚本 `src/main/resources/db/init.sql`（可重复执行，会重建全部表）。
- 数据库变更使用迁移脚本，禁止手工改生产库。

## 5. 上线检查清单

- [ ] 核心流程回归：浏览 → 标签筛选 → 详情 → 登录 → 收藏/取消 → 我的收藏
- [ ] 后台回归：歌单增改/上下架、标签、推荐位、封面上传
- [ ] 下架歌单详情返回 404（前台不可访问）
- [ ] 错误日志无堆积；`/sitemap.xml`、`/robots.txt` 可访问
- [ ] 备份完成（数据库 + 上传目录）

## 6. 回滚

保留上一版本 war 包；回滚 = 替换 war 并重启 Tomcat。数据库结构变更需准备对应回滚脚本。

## 7. 运行环境注意

- 需 JDK 11+ 与 Tomcat 9；命令行启动 Tomcat 时确保 `JAVA_HOME` 与 `CATALINA_HOME`/`CATALINA_BASE` 指向正确实例。
- Windows 下以服务方式运行 Tomcat/MySQL 时需要管理员权限。
- 普通用户访问 `/api/admin/**` 返回 403；未登录访问需登录接口返回 401。