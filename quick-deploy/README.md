# 快速部署包

本目录提供**免构建**的部署产物，适合直接把项目跑起来验收。

| 文件 | 说明 |
|---|---|
| `music-web.war` | 预构建 WAR 包（JDK 17+，Tomcat 10.1） |
| `init.sql` | 数据库初始化脚本（会重建 `music_web` 库） |

校验值：

```
SHA-256 music-web.war  c17a9a407e5aadae1cc077cf970f0f0c0f822ee3b80a4563f8830da16ab03c98
SHA-256 init.sql       89ed494db38dc425f39cf8afc4b935c73ff7ee6d06c99b8118d218d1ef7ecada
```

## 环境要求

- JDK 17 或更高版本
- MySQL 8.0
- Tomcat **10.1**（Jakarta EE 10；Tomcat 9 不能运行）

## 三步启动

### 1. 初始化数据库

```bash
mysql -uroot -p < init.sql
```

> 脚本包含 `DROP TABLE`，会清空并重建 `music_web` 的全部表和种子数据。

### 2. 部署 WAR

把 `music-web.war` 复制到 Tomcat 的 `webapps/` 目录，然后启动 Tomcat。

### 3. 访问

```
http://localhost:8080/music-web/
```

演示账号：`admin / admin123`（管理员，可进入运营后台）。

## 数据库密码不是空怎么办？

预构建 WAR 内置的 `db.properties` 使用 `root` / 空密码。如果你的 MySQL root 设置了密码，需要：

1. 修改项目根目录的 `src/main/resources/db.properties`：

   ```properties
   db.username=root
   db.password=你的密码
   ```

2. 重新构建（项目根目录执行）：

   ```bash
   # Windows
   .\mvnw.cmd package
   # macOS / Linux
   ./mvnw package
   ```

3. 使用新生成的 `target/music-web.war` 部署。

## 其他说明

- 封面上传目录默认为 `<catalina.base>/music-web-uploads/covers`，Tomcat 进程需要写权限；也可以在 `db.properties` 中通过 `upload.dir` 指定其他目录（修改后需重新构建）。
- 完整开发/部署说明见项目根目录 [README.md](../README.md)，以及 [docs/deploy.md](../docs/deploy.md)、[docs/api.md](../docs/api.md)。
