<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>music-web · 自然音乐歌单</title>
  <link rel="icon" href="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M4 20c9 1 16-5.5 16-16-8.5-1-15.5 4-16 12 0 1.4.2 2.8 0 4z' fill='%237ce3ab'/%3E%3C/svg%3E">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/theme.css">
  <style>
    .wrap { max-width: 1080px; margin: 0 auto; padding: 120px 24px 80px; }
    .hero-lite h1 { margin: 0 0 16px; font-size: clamp(34px, 5.2vw, 56px); font-weight: 300; letter-spacing: .04em; line-height: 1.2; }
    .hero-lite p { margin: 0 0 34px; color: var(--txt2); font-size: 15px; line-height: 1.9; max-width: 640px; }
    .entries { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 20px; margin-top: 46px; }
    .entry { display: block; padding: 26px 24px; border-radius: var(--r-panel); color: var(--txt); transition: transform .35s var(--ease), border-color .35s; }
    .entry:hover { transform: translateY(-4px); border-color: rgba(124,227,171,.4); color: var(--txt); }
    .entry h3 { margin: 0 0 8px; font-size: 18px; font-weight: 600; }
    .entry p { margin: 0; font-size: 13px; color: var(--txt3); line-height: 1.8; }
    .entry .badge { margin-bottom: 14px; }
    .meta-line { margin-top: 44px; font-size: 12.5px; color: var(--txt3); }
  </style>
</head>
<body>

<nav class="nav scrolled">
  <div class="nav-inner">
    <a class="brand" href="${pageContext.request.contextPath}/">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="M4 20c9 1 16-5.5 16-16-8.5-1-15.5 4-16 12 0 1.4.2 2.8 0 4z"/><path d="M4 20C7 14 11 10.5 15.5 8.5"/></svg>
      自然音乐歌单
    </a>
    <div class="nav-links">
      <a href="${pageContext.request.contextPath}/hello">/hello</a>
      <a href="${pageContext.request.contextPath}/playlist">歌单列表</a>
      <c:choose>
        <c:when test="${not empty sessionScope.userId}">
          <div class="nav-user">
            <span class="nav-name"><c:out value="${sessionScope.username}"/></span>
            <a href="${pageContext.request.contextPath}/logout">注销</a>
          </div>
        </c:when>
        <c:otherwise>
          <a href="${pageContext.request.contextPath}/login">登录</a>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</nav>

<main class="wrap">
  <section class="hero-lite">
    <p class="badge">JavaEE8 · javax.servlet · Tomcat9</p>
    <h1>把世界调成静音，<br>听自然开口说话</h1>
    <p>
      这是《Java EE 企业级开发》课程的训练工程 music-web。业务模型与界面规范沿用个人项目
      music-web，但技术栈固定为 JavaEE8（javax.*）+ JDBC + Tomcat9，用来完成实验一至实验三的基础训练。
    </p>
  </section>

  <div class="entries">
    <a class="entry glass" href="${pageContext.request.contextPath}/hello?name=张亮">
      <span class="badge">实验一</span>
      <h3>Servlet 生命周期</h3>
      <p>访问 /hello，查看服务器时间、上下文路径与容器信息，并在 localhost 日志中观察 init / service / destroy 的调用顺序。</p>
    </a>
    <a class="entry glass" href="${pageContext.request.contextPath}/playlist">
      <span class="badge">实验二</span>
      <h3>歌单 CRUD</h3>
      <p>Servlet + Service + JDBC DAO + JSP 分层实现歌单列表与新增，包含标题唯一、状态合法等业务校验。</p>
    </a>
    <a class="entry glass" href="${pageContext.request.contextPath}/login">
      <span class="badge">实验三</span>
      <h3>登录与会话</h3>
      <p>HttpSession 保存登录态，Filter 拦截未登录访问，JSTL/EL 改造视图，注销调用 session.invalidate()。</p>
    </a>
  </div>

  <p class="meta-line">运行环境：JDK 11.0.27 · Maven 3.9.14 · Tomcat 9.0.117 · MySQL 8.0.46</p>
</main>

</body>
</html>