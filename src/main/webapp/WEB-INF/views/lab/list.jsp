<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>歌单管理（实验演示） · 自然音乐歌单</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/theme.css">
<style>
.wrap { max-width: 1080px; margin: 0 auto; padding: 104px 24px 70px; }
.panel { border-radius: var(--r-panel); padding: 30px 30px 34px; }
.panel h1 { margin: 0 0 6px; font-size: 25px; font-weight: 600; letter-spacing: .03em; }
.sub { margin: 0; font-size: 13px; color: var(--txt3); }
.actions { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; margin-bottom: 22px; flex-wrap: wrap; }
table { width: 100%; border-collapse: separate; border-spacing: 0; margin-top: 6px;
        background: rgba(255,255,255,.028); border: 1px solid var(--line);
        border-radius: var(--r-card); overflow: hidden; }
th, td { padding: 12px 14px; font-size: 13px; text-align: left; border-bottom: 1px solid var(--line); vertical-align: middle; }
th { color: var(--txt3); font-weight: 600; background: rgba(255,255,255,.03); }
tr:last-child td { border-bottom: none; }
.badge { display: inline-block; padding: 3px 10px; border-radius: 999px; font-size: 12px; }
.badge.on { color: var(--acc); background: var(--acc-dim); }
.badge.off { color: var(--txt3); background: rgba(255,255,255,.06); }
.alert { padding: 12px 15px; margin-bottom: 18px; border-radius: 12px; font-size: 13.5px;
         border: 1px solid rgba(124,227,171,.3); background: rgba(124,227,171,.08); color: var(--acc); }
.alert.err { border-color: rgba(255,143,164,.32); background: rgba(255,143,164,.1); color: var(--rose); }
.nav-user { display: flex; align-items: center; gap: 10px; }
.nav-name { font-size: 13px; color: var(--txt2); }
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
      <a href="${pageContext.request.contextPath}/">返回首页</a>
      <a href="${pageContext.request.contextPath}/admin.html">运营后台</a>
      <div class="nav-user">
        <span class="nav-name"><c:out value="${sessionScope.username}"/></span>
        <button class="btn btn-ghost btn-sm" type="button" onclick="labLogout()">注销</button>
      </div>
    </div>
  </div>
</nav>

<main class="wrap">
  <div class="panel glass">
    <div class="actions">
      <div>
        <h1>歌单管理</h1>
        <p class="sub">实验二/三演示页：JDBC 查询 + JSP/JSTL/EL 渲染（课程实验页面，不属于产品导航）</p>
      </div>
      <a class="btn btn-primary" href="${pageContext.request.contextPath}/lab/playlists?action=new">新增歌单</a>
    </div>

    <c:if test="${not empty param.msg}">
      <div class="alert"><c:out value="${param.msg}"/></div>
    </c:if>
    <c:if test="${not empty error}">
      <div class="alert err"><c:out value="${error}"/></div>
    </c:if>

    <table>
      <thead>
        <tr>
          <th style="width:70px">编号</th>
          <th>标题</th>
          <th>描述</th>
          <th style="width:90px">状态</th>
          <th style="width:90px">收藏数</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach items="${playlists}" var="p">
          <tr>
            <td class="num">${p.id}</td>
            <td><c:out value="${p.title}"/></td>
            <td><c:out value="${p.description}"/></td>
            <td><span class="badge ${p.online ? 'on' : 'off'}">${p.online ? '上架' : '下架'}</span></td>
            <td class="num">${p.favoriteCount}</td>
          </tr>
        </c:forEach>
        <c:if test="${empty playlists}">
          <tr><td colspan="5" class="sub">暂无歌单</td></tr>
        </c:if>
      </tbody>
    </table>
    <p class="sub" style="margin-top:14px">当前共 ${playlists.size()} 条 · 数据与产品共用同一张 playlists 表</p>
  </div>
</main>

<script>
function labLogout() {
  fetch('${pageContext.request.contextPath}/api/auth/logout', { method: 'POST' })
    .then(function () { location.href = '${pageContext.request.contextPath}/login.html'; });
}
</script>
</body>
</html>