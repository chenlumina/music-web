# 实验四 · AI 辅助编程记录与正式选题（JavaEE8）

## 一、AI 工具与工作流
- AI 编程助手：Qoder Desktop（本会话即 AI 辅助过程，所有对话与代码生成真实可查）。
- 工作流：AI 生成 → 人工审查 → 运行验证 → 记录。每次 AI 产出都经本地编译、部署与 curl/浏览器回归后才采纳。
- 技术基线约束：JavaEE8（javax.*）+ Tomcat9 + MySQL8；禁止 jakarta.*、Tomcat10+、SpringBoot3。

## 二、AI 对话关键记录（prompt 摘要 / AI 输出 / 我的修改 / 修改理由）
1. prompt：在 music-lab 增加登录/会话/过滤器，沿用 music-web 业务逻辑但保持 JavaEE8。
   AI 输出：LoginServlet/LogoutServlet/AuthService/UserDao/EncodingFilter/AuthFilter。
   我的修改：把参考项目 music-web 的 jakarta.servlet.*、jakarta.persistence.* 导入改回 javax.servlet.*，持久层由 JPA 改 JDBC。
   理由：课程基线禁止 jakarta.*，Tomcat9 只认 javax.*；JPA/Hibernate 非本实验要求。
2. prompt：用 JSTL/EL 改造 JSP，减少脚本片段。
   AI 输出：c:forEach/c:if/c:choose/c:out/fmt:formatDate 视图。
   我的修改：标签库 URI 不照抄 music-web 的 jakarta.tags.core，改用 http://java.sun.com/jsp/jstl/core（JSTL1.2）。
   理由：jakarta.tags.core 属 JakartaEE10/JSTL3.0，Tomcat9+JSTL1.2 无法解析。
3. prompt：实验总结压到 200 字以内。
   AI 输出：初稿超字数。我的修改：压缩至 186/187/196 字。理由：报告要求 ≤200 字。
4. prompt：核对报告与源码、证据一致。
   AI 输出：Service 片段与修复后验证描述。我的修改：补 safeCover 空值处理、把"修复后验证"改挂真实截图证据。
   理由：证据纪律，不允许无证据的"成功/已实现"。

## 三、AI 建议 vs JavaEE8 基线冲突记录表
| AI/参考建议内容 | 冲突点 | 我的处置 | 验证结果 |
|---|---|---|---|
| 沿用 music-web 的 jakarta.servlet.* / jakarta.persistence.* 导入（AuthServlet/AuthService） | JavaEE8 基线禁止 jakarta.*；Tomcat9 不识别 | 改回 javax.servlet.*，JPA 改 JDBC PreparedStatement | mvn 编译通过、Tomcat9 部署与 13 项回归通过 |
| 沿用 music-web error.jsp 的 taglib uri="jakarta.tags.core" | JakartaEE10/JSTL3.0 URI，Tomcat9+JSTL1.2 不认 | 改 uri="http://java.sun.com/jsp/jstl/core" | JSTL 视图正常渲染（列表/错误页截图） |

## 四、正式选题登记表
- 题目：自然音乐网站
- 一句话功能描述：以自然声景为主题的歌单浏览与收藏网站，用户注册登录后浏览、收藏歌单，运营者上下架管理。
- 业务实体：用户 User、歌单 Playlist、收藏 Favorite（扩展：曲目 Track、标签 Tag）
- 核心用例：①注册/登录/注销 ②浏览歌单列表与详情 ③收藏/取消收藏 ④运营新增/上下架歌单 ⑤按标签筛选
- 技术栈：JavaEE8（javax.*）+ Tomcat9 + MySQL8 + JDBC（兼容 JavaEE8）+ JSP/JSTL/EL
- 部署目标：实验五先本地 Tomcat9 闭环，再 Docker（Tomcat9 镜像 + MySQL8 容器）或云服务器
- 实现载体说明：music-web 为只读参考（JakartaEE 写法与基线冲突）；课程合规实现为 music-lab（JavaEE8/javax）。

## 五、初始 MVP（AI 辅助，已运行验证）
核心实体「歌单」的列表查询与新增：PlaylistServlet → PlaylistService（标题唯一/状态合法校验）→ PlaylistDao（JDBC）→ JSP/JSTL 视图；PRG 模式。
证据：实验二/三功能截图与 curl 回归输出（列表、新增、新增后列表、数据库回读）。

## 六、下一阶段 3 个开发目标（对应实验五）
1. 完成"登录→浏览歌单→新增/编辑→看到更新"核心闭环，含真实业务规则（标题唯一、状态合法、上下架影响前台可见）。
2. 对 AI 生成代码做三层审查（正确性/安全性/可维护性），记录 ≥2 处审查后修改或拒绝。
3. 完成脱离 IDE 的外部部署（Docker 或云服务器）+ 可复现 DEPLOY.md + 异机访问验证。