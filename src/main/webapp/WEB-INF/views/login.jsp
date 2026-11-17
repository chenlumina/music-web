<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>登录 · 自然音乐歌单</title>
<link rel="icon" href="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M4 20c9 1 16-5.5 16-16-8.5-1-15.5 4-16 12 0 1.4.2 2.8 0 4z' fill='%237ce3ab'/%3E%3C/svg%3E">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/theme.css">
<style>
.auth { display: grid; grid-template-columns: 1.1fr 1fr; min-height: 100dvh; }
.auth-side { position: relative; overflow: hidden; display: flex; flex-direction: column; justify-content: space-between; padding: 40px 44px; }
.auth-bg { position: absolute; inset: 0; z-index: 0; }
.auth-bg img { width: 100%; height: 100%; object-fit: cover; }
.auth-bg::after { content: ""; position: absolute; inset: 0;
  background: linear-gradient(200deg, rgba(7,12,10,.2), rgba(7,12,10,.72) 82%),
              linear-gradient(90deg, rgba(7,12,10,.42), transparent 45%); }
.auth-side .brand { position: relative; z-index: 1; font-size: 18px; }
.auth-quote { position: relative; z-index: 1; }
.auth-quote h2 { margin: 0 0 10px; font-size: clamp(24px, 2.6vw, 32px); font-weight: 300; letter-spacing: .06em; line-height: 1.5; text-shadow: 0 2px 24px rgba(0,0,0,.45); }
.auth-quote p { margin: 0; font-size: 14px; color: var(--txt2); letter-spacing: .04em; }
.auth-main { display: grid; place-items: center; padding: 48px 24px; position: relative; }
.auth-main::before { content: ""; position: fixed; inset: 0; z-index: -1; pointer-events: none;
  background: radial-gradient(560px 420px at 82% 12%, rgba(124,227,171,.07), transparent 60%),
              radial-gradient(480px 380px at 12% 88%, rgba(242,192,120,.05), transparent 60%); }
.form-box { width: min(372px, 100%); }
.form-box > .head { margin-bottom: 26px; }
.form-box h1 { margin: 0 0 8px; font-size: 26px; font-weight: 700; letter-spacing: .03em; }
.form-box .sub { margin: 0; font-size: 13.5px; color: var(--txt3); }
.m-back { position: absolute; top: 22px; left: 22px; }
.field { margin-bottom: 16px; }
.go { width: 100%; margin-top: 8px; }
.msg { min-height: 20px; margin: 12px 0 0; font-size: 13px; color: var(--rose); }
.msg.ok { color: var(--acc); }
.hint { margin-top: 22px; padding: 11px 14px; border-radius: var(--r-input); font-size: 12.5px; color: var(--txt3); background: rgba(255,255,255,.035); border: 1px dashed var(--line-strong); }
.hint b { color: var(--txt2); font-weight: 600; }
@media (max-width: 760px) {
  .auth { grid-template-columns: 1fr; }
  .auth-side { min-height: 218px; padding: 24px 24px 28px; }
  .auth-quote h2 { font-size: 21px; }
  .auth-quote p { display: none; }
  .m-back { top: 18px; left: auto; right: 18px; }
}
</style>
</head>
<body>
<main class="auth">
  <aside class="auth-side">
    <div class="auth-bg"><img src="${pageContext.request.contextPath}/assets/img/login.jpg" alt=""></div>
    <a class="brand" href="${pageContext.request.contextPath}/">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="M4 20c9 1 16-5.5 16-16-8.5-1-15.5 4-16 12 0 1.4.2 2.8 0 4z"/><path d="M4 20C7 14 11 10.5 15.5 8.5"/></svg>
      自然音乐歌单
    </a>
    <div class="auth-quote">
      <h2>雨声、篝火、海浪，<br>今晚想听哪一种？</h2>
      <p>登录之后，把喜欢的歌单收进口袋</p>
    </div>
  </aside>

  <section class="auth-main">
    <a class="btn btn-ghost btn-sm m-back" href="${pageContext.request.contextPath}/">返回首页</a>
    <div class="form-box">
      <div class="head">
        <h1>欢迎回来</h1>
        <p class="sub">登录后可管理歌单，未登录访问会被过滤器拦回本页</p>
      </div>

      <form method="post" action="${pageContext.request.contextPath}/login">
        <div class="field">
          <label for="username">用户名</label>
          <input id="username" name="username" autocomplete="username" placeholder="3 到 20 位字母、数字或下划线" maxlength="20" value="<c:out value='${username}'/>" required>
        </div>
        <div class="field">
          <label for="password">密码</label>
          <input id="password" name="password" type="password" autocomplete="current-password" placeholder="至少 6 位" maxlength="64" required>
        </div>
        <button class="btn btn-primary go" type="submit">登录</button>
        <c:if test="${not empty error}"><p class="msg"><c:out value="${error}"/></p></c:if>
        <c:if test="${not empty param.msg}"><p class="msg ok"><c:out value="${param.msg}"/></p></c:if>
      </form>

      <div class="hint">演示账号：<b>admin / admin123</b>（管理员）· <b>test01 / test123</b>（普通用户）<br>没有账号？<a href="${pageContext.request.contextPath}/register">立即注册</a></div>
    </div>
  </section>
</main>
</body>
</html>