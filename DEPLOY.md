# music-web 部署文档（DEPLOY.md）

面向未参与开发的人：照本文可从零完成构建、数据库初始化、启动与验证。
技术基线：JavaEE8（javax.*）+ Tomcat9 + JDK11 + Maven3.8+ + MySQL8。禁止 jakarta.*/Tomcat10+。

## 0. 环境前提
- JDK 11（构建与运行），Maven 3.8+，MySQL 8（本地路径），或 Docker + Docker Compose（容器路径）。
- Tomcat 9.0.x（本地路径）。容器路径无需本机 Tomcat。
- 端口：应用 9531，MySQL 3306（容器路径可映射）。

## 1. 构建
```
set JAVA_HOME=<jdk11 路径>          # Windows；Linux 用 export
mvn clean package                  # 产出 target/music-web.war
```

## 2. 数据库初始化
```
mysql -u root -p < src/main/resources/db/init.sql        # 建库 music_web + playlists + 4 组测试数据
mysql -u root -p < src/main/resources/db/lab3-users.sql  # users 表 + admin/test01 种子
```
连接配置二选一：
- 本地：编辑 `src/main/resources/db.properties`（db.url/db.username/db.password），重新打包；
- 外置（推荐，容器/云）：设置环境变量 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`，优先于 db.properties。
  例：`DB_URL=jdbc:mysql://localhost:3306/music_web?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true`

## 3. 启动（路径 A：本地独立 Tomcat，非 IDE）
```
copy target\music-web.war <CATALINA_BASE>\webapps\      # Windows
# 或 cp target/music-web.war $CATALINA_BASE/webapps/    # Linux
<CATALINA_HOME>\bin\catalina.bat run                    # 或 bin/catalina.sh run
```
看到 `Deployment of web application archive [...music-web.war] has finished` 即部署成功。

## 3'. 启动（路径 B：Docker Compose，Tomcat9 + MySQL8）
```
docker compose up -d --build
```
- db 服务：mysql:8.0，自动执行 db/ 下初始化脚本（docker-entrypoint-initdb.d），healthcheck 就绪后 web 才启动。
- web 服务：基于 tomcat:9-jdk11 镜像，COPY WAR 到 webapps，环境变量 DB_URL 指向 db 主机。
- 仅当 Docker 守护进程运行时可用；本文档在守护进程未运行的环境只作准备，未实际 run。

## 4. 验证访问
- 首页：http://localhost:9531/music-web/
- 登录：http://localhost:9531/music-web/login （admin/admin123 管理员；test01/test123 普通用户）
- 后台列表（需登录）：/music-web/playlist ；新增 /playlist?action=new ；编辑 /playlist?action=edit&id=N
- 前台只读（无需登录，仅上架）：/music-web/front
- 健康检查：`curl -s -o /dev/null -w "%{http_code}" http://localhost:9531/music-web/login` 期望 200。
- 部署后一键验证：`bash verify-deploy.sh http://localhost:9531/music-web`（health/login/front 全 200 输出 DEPLOY OK）。
- 健康检查端点：`/music-web/health` 返回 200 `OK db=up` 表示应用与数据库均可用；503 `FAIL db=down` 表示数据库不可达。
- 异机访问：将 localhost 换为部署主机 IP/域名（容器/云需开放 9531 安全组）；从另一台电脑或手机打开同一 URL。

## 5. 常见问题
- 9531 被占用：日志 `java.net.BindException: Address already in use`；改 conf/server.xml 端口或停掉占用进程。
- 数据库连接失败：`Communications link failure`；检查 MySQL 是否启动、DB_URL 主机/端口、账号密码、容器路径主机应为 db 而非 localhost。
- WAR 未解压/上下文错：访问 404；确认 webapps 下有 music-web.war 或 music-web/，上下文为 /music-web。
- 中文乱码：确认 EncodingFilter 生效、数据库与连接串 utf8mb4；POST 体以 UTF-8 提交。
- JSTL 不解析：标签库 URI 必须为 http://java.sun.com/jsp/jstl/core（JSTL1.2），不要用 jakarta.tags.core。
- 出现 jakarta.* 报错：本工程为 JavaEE8/javax，若引入 jakarta 依赖请改回 javax.servlet-api(provided)。
- Tomcat 版本不对：Tomcat10+ 使用 jakarta 命名空间，本 WAR 基于 javax，必须用 Tomcat9。
- 健康检查 503 db=down：先确认 MySQL 8 在 3306 监听、DB_URL 可达，再看应用日志的 SQLException 原因链。
- 部署后白屏/500：查 logs/localhost.*.log 的异常栈首行，按 DaoException 包装的 cause 定位到具体 SQL 或连接。
- 上下文路径带错：WAR 名决定上下文，访问必须以 /music-web 开头；改名后需同步 DEPLOY.md 与验证脚本。

## 6. 当前验证状态（如实）
- 已验证：本地独立 Tomcat（非 IDE）构建/初始化/启动/闭环回归/截图。
- 待补：Docker 容器实际 run（守护进程未运行）、云/VM 部署与异机访问截图（环境未启用/未授权）。