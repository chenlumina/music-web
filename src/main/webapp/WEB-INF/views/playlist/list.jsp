<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>歌单列表 · music-web</title>
  <link rel="icon" href="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M4 20c9 1 16-5.5 16-16-8.5-1-15.5 4-16 12 0 1.4.2 2.8 0 4z' fill='%237ce3ab'/%3E%3C/svg%3E">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/theme.css">
  <style>
    .wrap { max-width: 1120px; margin: 0 auto; padding: 110px 24px 80px; }
    .head { display: flex; align-items: flex-end; justify-content: space-between; gap: 18px; flex-wrap: wrap; margin-bottom: 26px; }
    .head h1 { margin: 0 0 6px; font-size: 28px; font-weight: 600; letter-spacing: .03em; }
    .head .sub { font-size: 13px; color: var(--txt3); }
    .count { display: inline-grid; place-items: center; min-width: 26px; height: 22px; padding: 0 9px; margin-left: 8px;
             border-radius: 999px; vertical-align: 4px; background: var(--acc-dim); color: var(--acc); font-size: 12.5px; }
    .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(268px, 1fr)); gap: 20px; }
    .pl-card { border-radius: var(--r-card); overflow: hidden; display: flex; flex-direction: column;
               border: 1px solid var(--line); background: var(--panel-solid); box-shadow: var(--shadow-1); }
    .pl-cover { position: relative; aspect-ratio: 16 / 10; overflow: hidden; background: linear-gradient(150deg, #16241d, #0d1512); }
    .pl-cover img { width: 100%; height: 100%; object-fit: cover; }
    .pl-cover .badge { position: absolute; top: 10px; right: 10px; }
    .pl-body { padding: 16px 17px 18px; display: flex; flex-direction: column; gap: 8px; flex: 1; }
    .pl-body h3 { margin: 0; font-size: 16.5px; font-weight: 600; letter-spacing: .02em; }
    .pl-desc { margin: 0; font-size: 13px; color: var(--txt3); line-height: 1.75; flex: 1;
               display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
    .pl-foot { display: flex; align-items: center; justify-content: space-between; margin-top: 4px; font-size: 12px; color: var(--txt3); }
    .alert { display: flex; align-items: center; gap: 10px; padding: 13px 18px; margin-bottom: 22px;
             border-radius: 14px; font-size: 14px; border: 1px solid rgba(124,227,171,.28);
             background: var(--acc-dim); color: var(--acc); }
    .alert.err { border-color: rgba(255,143,164,.32); background: rgba(255,143,164,.1); color: var(--rose); }
    .pager { display: flex; align-items: center; justify-content: center; gap: 14px; margin-top: 30px; }
    .pager-info { font-size: 13px; color: var(--txt3); }
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
  <c:if test="${not empty param.msg}">
    <div class="alert">
      <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>
      <c:out value="${param.msg}"/>
    </div>
  </c:if>

  <div class="head">
    <div>
      <h1><c:choose><c:when test="${frontMode}">上架歌单</c:when><c:otherwise>全部歌单</c:otherwise></c:choose><span class="count num">${empty total ? 0 : total}</span></h1>
      <span class="sub"><c:choose><c:when test="${frontMode}">前台只读视角：仅展示上架歌单，无需登录</c:when><c:otherwise>后台运营视角：上下架歌单都会列出，前台只展示上架歌单</c:otherwise></c:choose></span>
    </div>
    <c:if test="${empty frontMode}">
      <a class="btn btn-primary" href="${pageContext.request.contextPath}/playlist?action=new">新增歌单</a>
    </c:if>
  </div>

  <div class="grid">
    <c:choose>
      <c:when test="${empty playlists}">
        <div class="empty-state">
          <h3>还没有歌单</h3>
          <p>点击右上角「新增歌单」写入第一条数据</p>
        </div>
      </c:when>
      <c:otherwise>
        <c:forEach items="${playlists}" var="p">
          <article class="pl-card">
            <div class="pl-cover">
              <c:if test="${not empty p.coverUrl}">
                <img src="${pageContext.request.contextPath}/assets/img/${p.coverUrl}" alt="<c:out value='${p.title}'/>">
              </c:if>
              <span class="badge ${p.online ? '' : 'off'}">${p.online ? '上架中' : '已下架'}</span>
            </div>
            <div class="pl-body">
              <h3><c:out value="${p.title}"/></h3>
              <p class="pl-desc"><c:out value="${empty p.description ? '暂无描述' : p.description}"/></p>
              <c:if test="${empty frontMode}">
                <a class="btn btn-ghost btn-sm" href="${pageContext.request.contextPath}/playlist?action=edit&id=${p.id}">编辑</a>
              </c:if>
              <div class="pl-foot">
                <span>#${p.id}</span>
                <span class="num"><fmt:formatDate value="${p.createdAtDate}" pattern="yyyy-MM-dd HH:mm"/></span>
              </div>
            </div>
          </article>
        </c:forEach>
      </c:otherwise>
    </c:choose>
  </div>

  <c:if test="${empty frontMode and totalPages > 1}">
    <div class="pager">
      <c:if test="${page > 1}">
        <a class="btn btn-ghost btn-sm" href="${pageContext.request.contextPath}/playlist?page=${page - 1}">上一页</a>
      </c:if>
      <span class="pager-info num">第 ${page} / ${totalPages} 页</span>
      <c:if test="${page < totalPages}">
        <a class="btn btn-ghost btn-sm" href="${pageContext.request.contextPath}/playlist?page=${page + 1}">下一页</a>
      </c:if>
    </div>
  </c:if>
</main>

</body>
</html>