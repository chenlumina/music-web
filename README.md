# music-web · 自然音乐网站（JavaEE8 实现）

《Java EE 企业级开发》课程个人项目「自然音乐网站」的 JavaEE8 合规实现。
技术栈固定为 JavaEE8（`javax.*`）+ Tomcat9 + MySQL8 + JDBC，不使用 `jakarta.*`、Tomcat10+、SpringBoot3。

## 技术栈
- JavaEE8：`javax.servlet.*` / `javax.servlet.jsp.*` / `javax.servlet.http.*`
- Tomcat 9.0.117、JDK 11、Maven 3.9+、MySQL 8（utf8mb4）
- 视图：JSP + JSTL 1.2 / EL；持久层：原生 JDBC（PreparedStatement）
- 密码：jBCrypt 0.4（BCrypt 摘要，禁止明文）

## 核心功能
- 歌单列表查询、新增、编辑更新（Controller—Service—DAO 分层，PRG 模式）
- 真实业务规则：标题唯一（编辑时排除自身）、状态仅 ONLINE/OFFLINE、下架后前台 /front 不可见
- 登录/注销（HttpSession）、未登录拦截（AuthFilter）、统一编码（EncodingFilter）
- 前台只读列表（/front，仅上架、无需登录）、健康检查（/health）
- 统一异常兜底（DaoException + error-page）、JSP 置于 WEB-INF/views 防绕过

## 本地开发启动
1. 初始化数据库：`mysql -u root < src/main/resources/db/init.sql` 与 `db/lab3-users.sql`（库名 music_web）
2. 配置 `src/main/resources/db.properties`（url/username/password，可被 DB_URL/DB_USERNAME/DB_PASSWORD 覆盖）
3. 构建：`mvn clean package`（JDK11）
4. 部署：将 `target/music-web.war` 放入 Tomcat9 `webapps/`，启动后访问 `http://localhost:8080/music-web/`
5. 演示账号：admin/admin123（ADMIN）、test01/test123（USER）
6. 部署后验证：`bash verify-deploy.sh http://localhost:8080/music-web`

## 部署
Docker（tomcat:9-jdk11）与 MySQL8 一键部署见 `DEPLOY.md`；部署工件含 Dockerfile、docker-compose.yml、verify-deploy.sh。

## 配对部署验证
（实验六/七完成后在此记录搭档独立部署结果与证据。）