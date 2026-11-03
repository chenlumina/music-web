# 部署与运维说明

## 1. 构建

```powershell
# 需要 JDK 17+（本机 JDK 位于 C:\jdk21，未设置全局 JAVA_HOME 时先指定）
$env:JAVA_HOME = 'C:\jdk21'
.\mvnw.cmd package -DskipTests
# 产出 target\music-web.war
```

## 2. 部署到 Tomcat 10.1

1. 将 `music-web.war` 复制到 Tomcat 的 `webapps/` 目录，启动即自动解压部署。
2. 访问 `http://<host>:<port>/music-web/`。
3. 本机现状（见下「环境注意事项」）：Tomcat 服务配置端口为 **8888**。

> IDEA exploded 部署（开发用）：IDEA 中 Redeploy 即可，context 为 `/music_web_war_exploded`。
> 注意 IDEA 实例与 Tomcat10 服务同用 8888 端口，**同时只能运行一个**。

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

## 7. 本机环境注意事项（开发机）

- `JAVA_HOME` 未设置：运行 mvnw 前需 `$env:JAVA_HOME='C:\jdk21'`。
- `CATALINA_HOME` 系统环境变量已失效：命令行启动 Tomcat 需显式 `$env:CATALINA_HOME='C:\tomcat'`。
- MySQL80 / Tomcat10 为 Windows 服务，启动需管理员权限。
