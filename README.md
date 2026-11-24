# music-web · 自然音乐网站

自然音乐主题的歌单站点。登录后可管理歌单，前台只展示上架歌单。

## 功能
- 登录 / 注销；未登录访问后台列表会被拦截到登录页
- 歌单列表、新增、编辑更新（标题唯一，状态 ONLINE / OFFLINE）
- 前台只读列表 `/front`：仅展示上架歌单，无需登录
- 健康检查 `/health`：应用与数据库可用时返回 200

## 技术栈
- JavaEE8：`javax.servlet.*` 4.0、JSP + JSTL 1.2 / EL
- 持久层：JDBC（mysql-connector-j 8.0.33）
- 数据库：MySQL 8
- 运行：Tomcat 9、JDK 11、Maven 3.9
- 密码：jBCrypt（BCrypt 摘要）

## 目录结构
```
src/main/java/com/chenlumina/musicweb
├─ web      Servlet 与 Filter
├─ service  业务校验
├─ dao      JDBC 持久层
├─ entity   实体
└─ util     DbUtil / 异常
src/main/webapp/WEB-INF/views   JSP 视图
src/main/resources/db           init.sql / lab3-users.sql
```

## 本地运行
1. 初始化数据库：`mysql -u root < src/main/resources/db/init.sql`，再执行 `db/lab3-users.sql`
2. 连接配置：`src/main/resources/db.properties`（也可用环境变量 DB_URL / DB_USERNAME / DB_PASSWORD）
3. 构建：`mvn clean package`
4. 部署：将 `target/music-web.war` 放入 Tomcat9 的 `webapps/`
5. 访问：http://localhost:9531/music-web/
6. 演示账号：admin / admin123、test01 / test123
7. 部署后验证：`bash verify-deploy.sh http://localhost:9531/music-web`

## 部署
见 `DEPLOY.md`；工件含 Dockerfile（tomcat:9-jdk11）、docker-compose.yml（MySQL 8）、verify-deploy.sh。

## 配对部署验证
（待补充）