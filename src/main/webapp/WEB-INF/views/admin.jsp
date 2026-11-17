<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>后台管理 · music-web</title>
  <link rel="icon" href="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M4 20c9 1 16-5.5 16-16-8.5-1-15.5 4-16 12 0 1.4.2 2.8 0 4z' fill='%237ce3ab'/%3E%3C/svg%3E">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/theme.css">
  <style>
    .wrap { max-width: 1120px; margin: 0 auto; padding: 110px 24px 80px; }
    h1 { margin: 0 0 18px; font-size: 26px; font-weight: 600; }
    .stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 14px; margin-bottom: 30px; }
    .stat { border: 1px solid var(--line); border-radius: var(--r-card); background: var(--panel-solid); padding: 16px 18px; }
    .stat .k { font-size: 12.5px; color: var(--txt3); }
    .stat .v { font-size: 24px; font-weight: 600; margin-top: 6px; }
    table { width: 100%; border-collapse: collapse; font-size: 13.5px; }
    th, td { text-align: left; padding: 10px 12px; border-bottom: 1px solid var(--line); }
    th { color: var(--txt3); font-weight: 600; font-size: 12.5px; }
    td .badge { margin-right: 6px; }
    .ops a { margin-right: 10px; font-size: 13px; }
    .no { color: var(--txt3); }
  </style>
</head>
<body>

<nav class="nav scrolled">
  <div class="nav-inner">
    <a class="brand" href="${pageContext.request.contextPath}/">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="M4 20c9 1 16-5.5 16-16-8.5-1-15.5 4-16 12 0 1.4.2 2.8 0 4z"/><path d="M4 20C7 14 11 10.5 15.5 8.5"/></svg>
      自然音乐歌单 · 后台
    </a>
    <div class="nav-links">
      <a href="${pageContext.request.contextPath}/playlist">歌单管理</a>
      <a href="${pageContext.request.contextPath}/front">前台</a>
      <div class="nav-user">
        <span class="nav-name"><c:out value="${sessionScope.username}"/></span>
        <a href="${pageContext.request.contextPath}/logout">注销</a>
      </div>
    </div>
  </div>
</nav>

<main class="wrap">
  <h1>后台管理</h1>

  <div class="stats">
    <div class="stat"><div class="k">歌单</div><div class="v num">${stats.playlists}</div></div>
    <div class="stat"><div class="k">用户</div><div class="v num">${stats.users}</div></div>
    <div class="stat"><div class="k">收藏</div><div class="v num">${stats.favorites}</div></div>
    <div class="stat"><div class="k">推荐</div><div class="v num">${stats.recommendations}</div></div>
    <div class="stat"><div class="k">曲目</div><div class="v num">${stats.tracks}</div></div>
    <div class="stat"><div class="k">标签</div><div class="v num">${stats.tags}</div></div>
  </div>

  <table>
    <thead>
      <tr><th>#</th><th>标题</th><th>状态</th><th>收藏</th><th>推荐</th><th>操作</th></tr>
    </thead>
    <tbody>
      <c:forEach items="${playlists}" var="p">
        <tr>
          <td class="num">${p.id}</td>
          <td><a href="${pageContext.request.contextPath}/playlist?action=detail&id=${p.id}"><c:out value="${p.title}"/></a></td>
          <td><span class="badge ${p.online ? '' : 'off'}">${p.online ? '上架' : '下架'}</span></td>
          <td class="num">${empty p.favoriteCount ? 0 : p.favoriteCount}</td>
          <td>
            <c:choose>
              <c:when test="${not empty recommendedIds and recommendedIds.contains(p.id)}"><span class="badge">已推荐</span></c:when>
              <c:otherwise><span class="no">—</span></c:otherwise>
            </c:choose>
          </td>
          <td class="ops">
            <a href="${pageContext.request.contextPath}/playlist?action=edit&id=${p.id}">编辑</a>
            <a href="${pageContext.request.contextPath}/recommend?action=${not empty recommendedIds and recommendedIds.contains(p.id) ? 'remove' : 'add'}&playlistId=${p.id}">${not empty recommendedIds and recommendedIds.contains(p.id) ? '取消推荐' : '推荐'}</a>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</main>

</body>
</html>