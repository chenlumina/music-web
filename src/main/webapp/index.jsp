<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>自然音乐歌单 · 把世界调成静音</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/theme.css">
<style>
/* ===== 首页专属：Hero / coverflow / 翻转卡片 ===== */
[hidden] { display: none !important; }

/* -- Hero -- */
.hero { position: relative; min-height: min(88vh, 880px); display: flex; align-items: center; overflow: hidden; }
.hero-bg {
  position: absolute; inset: -36px; z-index: 0; will-change: transform;
  transform: translate3d(calc(var(--hx, 0) * -22px), calc(var(--hy, 0) * -14px), 0) scale(1.06);
  transition: transform .35s ease-out;
}
.hero-bg img { width: 100%; height: 100%; object-fit: cover; }
.hero-tint {
  position: absolute; inset: 0; z-index: 1;
  background:
    linear-gradient(90deg, rgba(7,12,10,.84) 6%, rgba(7,12,10,.4) 55%, rgba(7,12,10,.22)),
    linear-gradient(180deg, rgba(7,12,10,.5), transparent 32%, transparent 60%, var(--bg0) 97%);
}
#ff { position: absolute; inset: 0; z-index: 2; width: 100%; height: 100%; pointer-events: none; }
.hero-inner {
  position: relative; z-index: 3; width: 100%; max-width: 1200px; margin: 0 auto;
  padding: 130px 24px 120px;
  transform: translate3d(calc(var(--hx, 0) * 10px), calc(var(--hy, 0) * 8px), 0);
  transition: transform .35s ease-out;
}
.hero-title { margin: 0 0 20px; font-size: clamp(40px, 6.6vw, 76px); line-height: 1.16; font-weight: 250; letter-spacing: .05em; }
.hw { display: inline-block; opacity: 0; transform: translateY(46px); animation: hw-in .95s var(--ease) forwards; animation-delay: calc(var(--i) * 95ms + .2s); }
@keyframes hw-in { to { opacity: 1; transform: none; } }
.hero-sub { margin: 0 0 36px; color: var(--txt2); font-size: clamp(14px, 1.6vw, 17px); letter-spacing: .03em; }
.hero-cta { display: flex; gap: 14px; flex-wrap: wrap; }

/* -- 区块 -- */
.sec { max-width: 1200px; margin: 0 auto; padding: 84px 24px 0; }
.sec-head { display: flex; align-items: baseline; justify-content: space-between; gap: 14px; margin-bottom: 24px; flex-wrap: wrap; }
.sec-head h2 { margin: 0; font-size: clamp(22px, 3vw, 30px); font-weight: 600; letter-spacing: .03em; }
.sec-sub { font-size: 13px; color: var(--txt3); }
.lib-count {
  display: inline-grid; place-items: center; min-width: 26px; height: 22px; padding: 0 9px;
  margin-left: 8px; border-radius: 999px; vertical-align: 4px;
  background: var(--acc-dim); color: var(--acc); font-size: 12.5px;
}
#library { scroll-margin-top: 84px; padding-bottom: 90px; }

/* -- Coverflow 推荐位 -- */
.cf-ctrl { display: flex; align-items: center; gap: 12px; }
.icon-btn {
  width: 40px; height: 40px; border-radius: 999px; display: grid; place-items: center;
  color: var(--txt2); cursor: pointer;
  background: rgba(255,255,255,.05); border: 1px solid var(--line-strong);
  transition: all .3s var(--ease);
}
.icon-btn svg { width: 18px; height: 18px; }
.icon-btn:hover:not(:disabled) { color: var(--acc-ink); background: var(--acc); border-color: var(--acc); transform: translateY(-2px); }
.icon-btn:disabled { opacity: .3; cursor: default; }
.cf-pos { font-size: 12px; color: var(--txt3); min-width: 44px; text-align: center; }
.cf-stage { position: relative; height: 296px; perspective: 1400px; outline: none; touch-action: pan-y; }
.cf-track { position: absolute; inset: 0; transform-style: preserve-3d; }
.cf-card {
  position: absolute; left: 50%; top: 6px;
  width: min(360px, 68vw); height: 272px; margin-left: calc(min(360px, 68vw) / -2);
  border-radius: var(--r-panel); overflow: hidden;
  background-size: cover; background-position: center;
  border: 1px solid var(--line-strong);
  box-shadow: var(--shadow-2);
  transform: translateX(var(--ox, 0)) translateZ(var(--oz, 0px)) rotateY(var(--oy, 0deg));
  opacity: var(--op, 1);
  transition: transform .7s var(--ease), opacity .7s var(--ease), border-color .4s;
  cursor: pointer;
}
.cf-card.active { border-color: rgba(124,227,171,.5); }
.cf-scrim { position: absolute; inset: 0; background: linear-gradient(180deg, transparent 46%, rgba(5,10,8,.84)); }
.cf-title { position: absolute; left: 20px; right: 20px; bottom: 16px; margin: 0; font-size: 17px; font-weight: 600; letter-spacing: .03em; }

/* -- 歌单卡片（3D 翻转 + 倾斜跟随） -- */
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 22px; }
.scene { perspective: 1200px; }
.card3d {
  position: relative; transform-style: preserve-3d;
  transform: rotateX(var(--rx, 0deg)) rotateY(calc(var(--ry, 0deg) + var(--flip, 0deg)));
  transition: transform .55s var(--ease);
}
.card3d.tilting { transition: none; }
.card3d.flipped { --flip: 180deg; transition: transform .78s var(--ease); }
.face {
  backface-visibility: hidden; -webkit-backface-visibility: hidden;
  border-radius: var(--r-card); overflow: hidden;
  border: 1px solid var(--line); background: var(--panel-solid);
  box-shadow: var(--shadow-1);
}
.face.front { position: relative; display: flex; flex-direction: column; }
.face.back {
  position: absolute; inset: 0; transform: rotateY(180deg);
  display: flex; flex-direction: column;
  background: linear-gradient(170deg, #14231c, #0e1813);
}
.cover { position: relative; aspect-ratio: 4 / 3; overflow: hidden; }
.cover img { width: 100%; height: 100%; object-fit: cover; transition: transform .6s var(--ease); }
.card3d:hover .cover img { transform: scale(1.07); }
.cover .heart-btn { position: absolute; top: 10px; right: 10px; z-index: 3; }
.peek {
  position: absolute; right: 12px; bottom: 12px; z-index: 3;
  display: inline-flex; align-items: center; gap: 6px;
  padding: 7px 13px; border-radius: 999px; font-size: 12px; color: var(--txt);
  background: rgba(8, 14, 11, .6); border: 1px solid var(--line-strong);
  -webkit-backdrop-filter: blur(10px); backdrop-filter: blur(10px);
  opacity: 0; transform: translateY(8px);
  transition: all .35s var(--ease);
}
.peek svg { width: 13px; height: 13px; color: var(--acc); }
.card3d:hover .peek, .card3d:focus-visible .peek { opacity: 1; transform: none; }
.glare {
  position: absolute; inset: 0; z-index: 4; border-radius: inherit; pointer-events: none;
  opacity: 0; transition: opacity .35s; mix-blend-mode: screen;
  background: radial-gradient(260px circle at var(--gx, 50%) var(--gy, 50%), rgba(255,255,255,.15), transparent 60%);
}
.card3d:hover .glare { opacity: 1; }
.meta { padding: 15px 16px 16px; display: flex; flex-direction: column; gap: 7px; }
.meta h3 { margin: 0; font-size: 16px; font-weight: 600; letter-spacing: .02em; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.meta .tags { margin: 0; font-size: 12px; color: var(--txt3); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.mrow { display: flex; align-items: center; justify-content: space-between; }
.favc { display: inline-flex; align-items: center; gap: 6px; font-size: 12.5px; color: var(--txt3); }
.favc svg { width: 14px; height: 14px; color: var(--rose); opacity: .85; }
/* 背面：曲目单 */
.bhead { display: flex; align-items: center; justify-content: space-between; gap: 10px; padding: 14px 16px 10px; }
.bhead h3 { margin: 0; font-size: 15px; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.bhead h3 a { color: var(--txt); text-decoration: none; }
.bhead h3 a:hover { color: var(--acc); }
.unflip {
  flex: none; width: 32px; height: 32px; border-radius: 999px; display: grid; place-items: center;
  color: var(--txt2); cursor: pointer; background: rgba(255,255,255,.06); border: 1px solid var(--line);
  transition: all .25s var(--ease);
}
.unflip svg { width: 15px; height: 15px; }
.unflip:hover { color: var(--acc); border-color: rgba(124,227,171,.4); }
.tracks { flex: 1; overflow-y: auto; margin: 0; padding: 2px 16px 8px; list-style: none; scrollbar-width: thin; }
.tracks li { display: flex; gap: 10px; align-items: baseline; padding: 8px 0; border-bottom: 1px solid var(--line); font-size: 13px; }
.tracks li:last-child { border-bottom: none; }
.tracks .no { color: var(--acc); font-size: 11px; min-width: 1.5em; }
.tracks .tt { flex: 1; color: var(--txt); }
.tracks .dur { color: var(--txt3); font-size: 12px; }
.t-hint { justify-content: center; border: none !important; color: var(--txt3); padding: 20px 0 !important; }
.t-err { color: var(--rose) !important; }
.bfoot { display: flex; align-items: center; justify-content: space-between; gap: 10px; padding: 12px 16px; border-top: 1px solid var(--line); }
.bstat { font-size: 12px; color: var(--txt3); }
/* 骨架屏 */
.sk-scene { border: 1px solid var(--line); border-radius: var(--r-card); background: var(--panel-solid); overflow: hidden; padding-bottom: 16px; }
.sk-cover { aspect-ratio: 4 / 3; border-radius: 0; }
.sk-l1 { height: 15px; width: 58%; margin: 15px 16px 9px; }
.sk-l2 { height: 11px; width: 38%; margin: 0 16px; }

/* -- 弹窗 -- */
.dlg-hero { position: relative; height: 200px; }
.dlg-hero img { width: 100%; height: 100%; object-fit: cover; }
.dlg-close { position: absolute; top: 12px; right: 12px; background: rgba(8,14,11,.55); -webkit-backdrop-filter: blur(8px); backdrop-filter: blur(8px); }
.dlg-body { padding: 18px 22px 4px; }
.dlg-body h3 { margin: 0 0 6px; font-size: 19px; font-weight: 700; letter-spacing: .02em; }
.dlg-tags { margin: 0 0 10px; font-size: 12.5px; color: var(--acc); }
.dlg-desc { margin: 0 0 6px; font-size: 13.5px; line-height: 1.75; color: var(--txt2); }
.dlg-tracks { max-height: 250px; padding: 4px 0 8px; }
.dlg-foot { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 14px 22px 18px; border-top: 1px solid var(--line); margin-top: 10px; }
.dlg-stat { display: block; font-size: 11.5px; color: var(--txt3); margin-top: 4px; }

@media (max-width: 640px) {
  .hero-inner { padding: 104px 20px 96px; }
  .cf-stage { height: 250px; }
  .cf-card { height: 226px; }
  .sec { padding-top: 64px; }
}
@media (prefers-reduced-motion: reduce) {
  .hw { animation: none; opacity: 1; transform: none; }
  #ff { display: none; }
}
</style>
</head>
<body>

<nav class="nav">
  <div class="nav-inner">
    <a class="brand" href="${pageContext.request.contextPath}/">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="M4 20c9 1 16-5.5 16-16-8.5-1-15.5 4-16 12 0 1.4.2 2.8 0 4z"/><path d="M4 20C7 14 11 10.5 15.5 8.5"/></svg>
      自然音乐歌单
    </a>
    <div class="nav-links" id="navUser"></div>
  </div>
</nav>

<header class="hero" id="hero">
  <div class="hero-bg"><img src="${pageContext.request.contextPath}/assets/img/hero.jpg" alt="" fetchpriority="high"></div>
  <div class="hero-tint"></div>
  <canvas id="ff" aria-hidden="true"></canvas>
  <div class="hero-inner">
    <h1 class="hero-title">
      <span class="hw" style="--i:0">把世界调成静音，</span><br>
      <span class="hw" style="--i:1">听自然开口说话</span>
    </h1>
    <p class="hero-sub hw" style="--i:2">雨声、溪流、鸟鸣与篝火，为专注、冥想与安眠准备的自然声歌单</p>
    <div class="hero-cta hw" style="--i:3">
      <a class="btn btn-primary" href="#library">开始聆听</a>
      <a class="btn btn-ghost" href="${pageContext.request.contextPath}/my-favorites.html">我的收藏</a>
    </div>
  </div>
</header>

<section class="sec" id="recsSec" hidden>
  <div class="sec-head">
    <div>
      <h2>编辑精选</h2>
      <span class="sec-sub" style="display:block;margin-top:5px">算法推荐即将上线 · 以下为运营配置的精选内容</span>
    </div>
    <div class="cf-ctrl">
      <button class="icon-btn" id="cfPrev" aria-label="上一位">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="M14.5 5.5 8 12l6.5 6.5"/></svg>
      </button>
      <span class="cf-pos num" id="cfPos"></span>
      <button class="icon-btn" id="cfNext" aria-label="下一位">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="m9.5 5.5 6.5 6.5-6.5 6.5"/></svg>
      </button>
    </div>
  </div>
  <div class="cf-stage" id="cfStage" tabindex="0" aria-label="推荐位轮播，可用左右方向键切换">
    <div class="cf-track" id="cfTrack"></div>
  </div>
</section>

<section class="sec" id="library">
  <div class="sec-head">
    <h2>全部歌单 <span class="lib-count num" id="libCount">0</span></h2>
    <span class="sec-sub">点开卡片，翻到背面看曲目单</span>
  </div>
  <div class="chips" id="chips" style="margin-bottom: 26px"></div>
  <div class="grid" id="grid"></div>
  <div class="pager" id="pager"></div>
</section>

<footer class="footer">
  <div class="brand">
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="M4 20c9 1 16-5.5 16-16-8.5-1-15.5 4-16 12 0 1.4.2 2.8 0 4z"/><path d="M4 20C7 14 11 10.5 15.5 8.5"/></svg>
    自然音乐歌单
  </div>
  <p>雨声、溪流、鸟鸣与篝火 · Java EE 课程实践项目</p>
</footer>

<dialog class="modal" id="plDlg" aria-label="歌单详情">
  <div class="dlg-hero">
    <img id="dlgCover" alt="歌单封面">
    <button class="icon-btn dlg-close" aria-label="关闭">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="m6 6 12 12M18 6 6 18"/></svg>
    </button>
  </div>
  <div class="dlg-body">
    <h3 id="dlgTitle"></h3>
    <p class="dlg-tags" id="dlgTags"></p>
    <p class="dlg-desc" id="dlgDesc"></p>
    <ol class="tracks dlg-tracks" id="dlgTracks"></ol>
  </div>
  <div class="dlg-foot">
    <div>
      <span class="favc" id="dlgFavc"></span>
      <span class="dlg-stat" id="dlgStat"></span>
    </div>
    <div style="display:flex;gap:10px;align-items:center">
      <a class="btn btn-ghost btn-sm" id="dlgDetail" href="#">查看完整详情</a>
      <button class="btn btn-primary btn-sm" id="dlgFav"></button>
    </div>
  </div>
</dialog>

<script>window.APP_BASE = '${pageContext.request.contextPath}';</script>
<script src="${pageContext.request.contextPath}/assets/app.js"></script>
<script src="${pageContext.request.contextPath}/assets/index.js"></script>
</body>
</html>
