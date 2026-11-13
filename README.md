# music-lab · 自然音乐网站（JavaEE8 课程实现）

《Java EE 企业级开发》课程个人项目「自然音乐网站」的 JavaEE8 合规实现载体。
业务模型与界面规范沿用只读参考项目 music-web；因课程基线禁止 JakartaEE，
本工程固定为 JavaEE8（`javax.*`）+ Tomcat9 + MySQL8 + JDBC，不使用 `jakarta.*`、Tomcat10+、SpringBoot3。

## 技术栈
- JavaEE8：`javax.servlet.*` / `javax.servlet.jsp.*` / `javax.servlet.http.*`
- Tomcat 9.0.117、JDK 11、Maven 3.9+、MySQL 8（utf8mb4）
- 视图：JSP + JSTL 1.2 / EL；持久层：原生 JDBC（PreparedStatement）
- 密码：jBCrypt 0.4（BCrypt 摘要，禁止明文）

## 核心功能
- 歌单列表查询与新增（Controller—Service—DAO 分层，PRG 模式）
- 业务校验：标题必填且唯一、状态仅 ONLINE/OFFLINE
- 登录/注销（HttpSession）、未登录拦截（AuthFilter）、统一编码（EncodingFilter）
- 统一异常兜底（DaoException + error-page）、JSP 置于 WEB-INF/views 防绕过

## 本地开发启动
1. 初始化数据库：`mysql -u root < src/main/resources/db/init.sql` 与 `db/lab3-users.sql`
2. 配置 `src/main/resources/db.properties`（url/username/password）
3. 构建：`mvn clean package`（JDK11）
4. 部署：将 `target/music-lab.war` 放入 Tomcat9 `webapps/`，启动后访问 `http://localhost:8080/music-lab/`
5. 演示账号：admin/admin123（ADMIN）、test01/test123（USER）

## 部署
外部部署（Docker/云服务器）与可复现 `DEPLOY.md` 见实验五交付。

## 配对部署验证
（实验六/七完成后在此记录搭档独立部署结果与证据。）