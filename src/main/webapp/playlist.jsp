<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title><c:out value="${p.title}"/> · 自然音乐歌单</title>
<meta name="description" content="<c:out value='${p.description}'/>">
<meta property="og:type" content="website">
<meta property="og:title" content="<c:out value='${p.title}'/> · 自然音乐歌单">
<meta property="og:description" content="<c:out value='${p.description}'/>">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/theme.css">
<style>
[hidden] { display: none !important; }
.pl-wrap { max-width: 980px; margin: 0 auto; padding: 106px 24px 70px; }
.pl-hero { display: grid; grid-template-columns: 300px 1fr; gap: 30px; align-items: start; }
.pl-cover {
  border-radius: var(--r-panel); overflow: hidden; border: 1px solid var(--line-strong);
  box-shadow: var(--shadow-2); aspect-ratio: 1/1; background: var(--bg2);
}
.pl-cover img { width: 100%; height: 100%; object-fit: cover; }
.pl-info h1 { margin: 0 0 14px; font-size: clamp(26px, 3.4vw, 38px); font-weight: 700; letter-spacing: .03em; }
.pl-tags { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 18px; }
.pl-desc {
  margin: 0 0 22px; color: var(--txt2); font-size: 15px; line-height: 1.9;
  white-space: pre-line; max-width: 60ch;
}
.pl-act { display: flex; align-items: center; gap: 16px; }
.pl-favc { display: inline-flex; align-items: center; gap: 7px; font-size: 14px; color: var(--txt2); }
.pl-favc svg { width: 16px; height: 16px; color: var(--rose); }
.trk-sec { margin-top: 46px; }
.trk-sec h2 { margin: 0 0 6px; font-size: 20px; font-weight: 600; letter-spacing: .03em; }
.trk-sec .sub { margin: 0 0 18px; font-size: 13px; color: var(--txt3); }
.tracks { border-radius: var(--r-card); overflow: hidden; border: 1px solid var(--line); background: rgba(255,255,255,.028); }
.tracks li { display: flex; gap: 14px; align-items: baseline; padding: 13px 18px; border-bottom: 1px solid var(--line); font-size: 14px; list-style: none; }
.tracks li:last-child { border-bottom: none; }
.tracks .no { color: var(--acc); font-size: 12px; min-width: 2em; }
.tracks .tt { flex: 1; }
.tracks .dur { color: var(--txt3); font-size: 13px; }
@media (max-width: 720px) {
  .pl-hero { grid-template-columns: 1fr; }
  .pl-cover { max-width: 280px; }
}
</style>
</head>
<body data-pl-id="${p.id}" data-fav-count="${p.favoriteCount}">

<nav class="nav">
  <div class="nav-inner">
    <a class="brand" href="${pageContext.request.contextPath}/">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="M4 20c9 1 16-5.5 16-16-8.5-1-15.5 4-16 12 0 1.4.2 2.8 0 4z"/><path d="M4 20C7 14 11 10.5 15.5 8.5"/></svg>
      自然音乐歌单
    </a>
    <div class="nav-links">
      <a href="${pageContext.request.contextPath}/">返回首页</a>
      <a href="${pageContext.request.contextPath}/my-favorites.html">我的收藏</a>
    </div>
  </div>
</nav>

<main class="pl-wrap">
  <section class="pl-hero">
    <div class="pl-cover"><img src="<c:out value='${cover}'/>" alt="《<c:out value='${p.title}'/>》封面"></div>
    <div class="pl-info">
      <h1><c:out value="${p.title}"/></h1>
      <div class="pl-tags">
        <c:forEach var="t" items="${p.tags}">
          <span class="badge"><c:out value="${t}"/></span>
        </c:forEach>
      </div>
      <p class="pl-desc"><c:out value="${p.description}"/></p>
      <div class="pl-act">
        <button class="heart-btn" id="favBtn" aria-label="收藏"></button>
        <span class="pl-favc" id="favc"></span>
      </div>
    </div>
  </section>

  <section class="trk-sec">
    <h2>曲目单</h2>
    <p class="sub">播放功能为占位，本阶段仅展示曲目信息</p>
    <ol class="tracks">
      <c:forEach var="t" items="${p.tracks}">
        <li>
          <b class="no num">${t.trackNo}</b>
          <span class="tt"><c:out value="${t.title}"/></span>
          <span class="dur num"><c:out value="${t.dur}"/></span>
        </li>
      </c:forEach>
    </ol>
  </section>
</main>

<footer class="footer">
  <p>雨声、溪流、鸟鸣与篝火 · Java EE 课程实践项目</p>
</footer>

<script type="application/ld+json">${jsonLd}</script>
<script>window.APP_BASE = '${pageContext.request.contextPath}';</script>
<script src="${pageContext.request.contextPath}/assets/app.js"></script>
<script>
(function () {
  'use strict';
  const M = window.MUSIC;
  const btn = document.getElementById('favBtn');
  const favc = document.getElementById('favc');
  const id = +document.body.dataset.plId;
  let count = +document.body.dataset.favCount || 0;
  let me = null;
  let fav = false;

  function paint() {
    btn.classList.toggle('on', fav);
    btn.innerHTML = M.ICON.heart(fav);
    favc.innerHTML = M.ICON.heart(true) + '<b class="num">' + count + '</b> 人收藏';
  }

  async function boot() {
    paint();
    try {
      const r = await M.getJSON(M.BASE + '/api/auth/me');
      me = r.code === 0 ? r.data : null;
    } catch (e) { me = null; }
    if (!me) return;
    try {
      const r = await M.getJSON(M.BASE + '/api/me/favorites?page=1&size=50');
      if (r.code === 0) fav = r.data.list.some(f => f.playlistId === id);
    } catch (e) { /* 状态加载失败保持未收藏显示 */ }
    paint();
  }

  btn.addEventListener('click', async () => {
    if (!me) {
      location.href = M.BASE + '/login.html?redirect=' + encodeURIComponent(location.pathname + location.search);
      return;
    }
    btn.disabled = true;
    try {
      const r = await M.toggleFav(id, fav);
      if (r.code === 401) {
        location.href = M.BASE + '/login.html?redirect=' + encodeURIComponent(location.pathname + location.search);
        return;
      }
      if (r.code !== 0) { M.toast(r.message, false); return; }
      fav = !fav;
      count += fav ? 1 : -1;
      paint();
      M.toast(fav ? '已加入收藏' : '已取消收藏');
    } catch (e) {
      M.toast('网络异常，请重试', false);
    } finally {
      btn.disabled = false;
    }
  });

  M.navScrollWatch();
  boot();
})();
</script>
</body>
</html>
