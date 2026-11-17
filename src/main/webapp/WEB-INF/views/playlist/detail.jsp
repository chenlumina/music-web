<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>歌单详情 · music-web</title>
  <link rel="icon" href="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M4 20c9 1 16-5.5 16-16-8.5-1-15.5 4-16 12 0 1.4.2 2.8 0 4z' fill='%237ce3ab'/%3E%3C/svg%3E">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/theme.css">
  <style>
    .wrap { max-width: 980px; margin: 0 auto; padding: 110px 24px 80px; }
    .dcard { display: grid; grid-template-columns: 340px 1fr; border-radius: var(--r-panel); overflow: hidden;
             border: 1px solid var(--line); background: var(--panel-solid); box-shadow: var(--shadow-1); }
    .dcover { background: linear-gradient(150deg, #16241d, #0d1512); min-height: 280px; }
    .dcover img { width: 100%; height: 100%; object-fit: cover; }
    .dinfo { padding: 28px 28px 30px; display: flex; flex-direction: column; gap: 12px; }
    .dinfo h1 { margin: 0; font-size: 26px; font-weight: 600; letter-spacing: .02em; }
    .ddesc { margin: 0; color: var(--txt2); line-height: 1.9; white-space: pre-wrap; }
    .dmeta { display: flex; gap: 16px; flex-wrap: wrap; color: var(--txt3); font-size: 12.5px; }
    .dactions { display: flex; gap: 12px; flex-wrap: wrap; margin-top: 8px; }
    .dtags { display: flex; gap: 8px; flex-wrap: wrap; }
    @media (max-width: 760px) { .dcard { grid-template-columns: 1fr; } .dcover { min-height: 210px; } }
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
      <a href="${pageContext.request.contextPath}/front">前台列表</a>
      <c:choose>
        <c:when test="${not empty sessionScope.userId}">
          <div class="nav-user">
            <a href="${pageContext.request.contextPath}/my-favorites">我的收藏</a>
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
  <div class="dcard">
    <div class="dcover">
      <c:if test="${not empty playlist.coverUrl}">
        <img src="${pageContext.request.contextPath}/assets/img/${playlist.coverUrl}" alt="<c:out value='${playlist.title}'/>">
      </c:if>
    </div>
    <div class="dinfo">
      <span class="badge ${playlist.online ? '' : 'off'}">${playlist.online ? '上架中' : '已下架'}</span>
      <h1><c:out value="${playlist.title}"/></h1>
      <p class="ddesc"><c:out value="${empty playlist.description ? '暂无描述' : playlist.description}"/></p>
      <div class="dmeta">
        <span>#${playlist.id}</span>
        <span>♥ ${empty playlist.favoriteCount ? 0 : playlist.favoriteCount} 人收藏</span>
        <span class="num"><fmt:formatDate value="${playlist.createdAtDate}" pattern="yyyy-MM-dd HH:mm"/></span>
      </div>
      <c:if test="${not empty playlistTags}">
        <div class="dtags">
          <c:forEach items="${playlistTags}" var="t"><span class="badge">#<c:out value="${t.name}"/></span></c:forEach>
        </div>
      </c:if>
      <div class="dactions">
        <c:choose>
          <c:when test="${not empty sessionScope.userId}">
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/favorite?playlistId=${playlist.id}">${favorited ? '取消收藏' : '收藏'}</a>
          </c:when>
          <c:otherwise>
            <a class="btn btn-ghost" href="${pageContext.request.contextPath}/login">登录后收藏</a>
          </c:otherwise>
        </c:choose>
        <c:if test="${adminMode}">
          <a class="btn btn-ghost" href="${pageContext.request.contextPath}/playlist?action=edit&id=${playlist.id}">编辑</a>
        </c:if>
        <a class="btn btn-ghost" href="${pageContext.request.contextPath}/${adminMode ? 'playlist' : 'front'}">返回列表</a>
      </div>
    </div>
  </div>
</main>

</body>
</html>