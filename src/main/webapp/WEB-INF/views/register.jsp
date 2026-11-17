<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>注册 · 自然音乐歌单</title>
  <link rel="icon" href="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M4 20c9 1 16-5.5 16-16-8.5-1-15.5 4-16 12 0 1.4.2 2.8 0 4z' fill='%237ce3ab'/%3E%3C/svg%3E">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/theme.css">
  <style>
    .wrap { max-width: 520px; margin: 0 auto; padding: 120px 24px 80px; }
    .panel { border-radius: var(--r-panel); padding: 34px 32px 36px; }
    .panel h1 { margin: 0 0 6px; font-size: 25px; font-weight: 600; letter-spacing: .03em; }
    .panel .sub { margin: 0 0 24px; font-size: 13px; color: var(--txt3); }
    .field { margin-bottom: 16px; }
    .alert { display: flex; align-items: center; gap: 10px; padding: 13px 16px; margin-bottom: 18px;
             border-radius: 12px; font-size: 13.5px; border: 1px solid rgba(255,143,164,.32);
             background: rgba(255,143,164,.1); color: var(--rose); }
    .foot { margin-top: 18px; font-size: 13px; color: var(--txt3); }
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
      <a href="${pageContext.request.contextPath}/login">登录</a>
    </div>
  </div>
</nav>

<main class="wrap">
  <div class="panel glass">
    <h1>创建账号</h1>
    <p class="sub">用户名 3-20 位字母、数字或下划线，密码 6-64 位</p>

    <c:if test="${not empty error}">
      <div class="alert">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 8v5M12 16h.01"/></svg>
        <c:out value="${error}"/>
      </div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/register">
      <div class="field">
        <label for="username">用户名</label>
        <input id="username" name="username" autocomplete="username" maxlength="20"
               value="<c:out value='${username}'/>" required>
      </div>
      <div class="field">
        <label for="password">密码</label>
        <input id="password" name="password" type="password" autocomplete="new-password"
               minlength="6" maxlength="64" required>
      </div>
      <button class="btn btn-primary" type="submit" style="width:100%">注册</button>
    </form>

    <p class="foot">已有账号？<a href="${pageContext.request.contextPath}/login">去登录</a></p>
  </div>
</main>
</body>
</html>