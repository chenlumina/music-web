/* ============================================================
   自然音乐歌单 · 首页交互
   视差 Hero + 萤火虫粒子 / 3D coverflow 推荐位 / 3D 翻转歌单卡
   （独立静态文件：JSP 会把 ${} 解析为 EL，故 JS 不内联在 JSP 里）
   ============================================================ */
(function () {
  'use strict';
  const M = window.MUSIC;
  const { esc, toast, ICON, coverFor, reveal, tilt, fireflies, getJSON, fmtDur, toggleFav } = M;
  const API = M.API;
  const $ = s => document.querySelector(s);

  let me = null;
  let favSet = {};
  const counts = {};
  let curTag = '', curPage = 1;
  const detailCache = new Map();
  let dlgId = null;

  /* ================= 用户态 ================= */
  function paintNav() {
    const box = $('#navUser');
    if (me) {
      const admin = me.role === 'ADMIN' ? '<a href="' + M.BASE + '/admin.html">运营后台</a>' : '';
      box.innerHTML =
        '<a href="' + M.BASE + '/my-favorites.html">我的收藏</a>' + admin +
        '<span class="nav-user"><span class="avatar">' + esc((me.username || '?')[0].toUpperCase()) + '</span>' +
        '<span class="nav-name">' + esc(me.username) + '</span></span>' +
        '<button class="btn btn-ghost btn-sm" id="logoutBtn">退出</button>';
      $('#logoutBtn').onclick = async () => {
        await fetch(API + '/auth/logout', { method: 'POST' });
        location.reload();
      };
    } else {
      const back = encodeURIComponent(location.pathname + location.search);
      box.innerHTML = '<a href="' + M.BASE + '/my-favorites.html">我的收藏</a>' +
        '<a class="btn btn-primary btn-sm" style="margin-left:6px" href="' + M.BASE + '/login.html?redirect=' + back + '">登录 / 注册</a>';
    }
  }

  async function loadMe() {
    try {
      const r = await getJSON(API + '/auth/me');
      me = r.code === 0 ? r.data : null;
    } catch (e) { me = null; }
    paintNav();
    if (!me) return;
    try {
      const r = await getJSON(API + '/me/favorites?page=1&size=50');
      if (r.code === 0) favSet = Object.fromEntries(r.data.list.filter(f => f.available).map(f => [f.playlistId, true]));
    } catch (e) { /* 收藏态缺失不影响浏览 */ }
  }

  /* ================= Hero 视差 ================= */
  function heroParallax() {
    if (M.REDUCED || !window.matchMedia('(pointer: fine)').matches) return;
    const hero = $('#hero');
    let raf = 0;
    hero.addEventListener('pointermove', e => {
      const r = hero.getBoundingClientRect();
      const x = (e.clientX - r.left) / r.width - .5;
      const y = (e.clientY - r.top) / r.height - .5;
      cancelAnimationFrame(raf);
      raf = requestAnimationFrame(() => {
        hero.style.setProperty('--hx', x.toFixed(3));
        hero.style.setProperty('--hy', y.toFixed(3));
      });
    });
    hero.addEventListener('pointerleave', () => {
      hero.style.setProperty('--hx', '0');
      hero.style.setProperty('--hy', '0');
    });
  }

  /* ================= 推荐位：3D coverflow ================= */
  let recs = [], recIdx = 0;

  function updateCf() {
    const cards = [...$('#cfTrack').children];
    cards.forEach((c, i) => {
      const o = i - recIdx;
      c.classList.toggle('active', o === 0);
      c.style.setProperty('--ox', (o * 46) + '%');
      c.style.setProperty('--oz', (-Math.min(Math.abs(o), 3) * 130) + 'px');
      c.style.setProperty('--oy', (-o * 30) + 'deg');
      c.style.setProperty('--op', Math.abs(o) > 2 ? '0' : (o === 0 ? '1' : '.5'));
      c.style.zIndex = String(10 - Math.abs(o));
      c.style.pointerEvents = Math.abs(o) > 2 ? 'none' : 'auto';
    });
    $('#cfPrev').disabled = recIdx <= 0;
    $('#cfNext').disabled = recIdx >= recs.length - 1;
    $('#cfPos').textContent = recs.length ? (recIdx + 1) + ' / ' + recs.length : '';
  }

  async function loadRecs() {
    let list = [];
    try {
      const r = await getJSON(API + '/recommend');
      if (r.code === 0) list = r.data || [];
    } catch (e) { /* 推荐位拉取失败静默降级 */ }
    recs = list;
    const sec = $('#recsSec');
    if (!list.length) { sec.hidden = true; return; }
    sec.hidden = false;
    const track = $('#cfTrack');
    list.forEach((rec, i) => {
      const c = document.createElement('div');
      c.className = 'cf-card';
      c.dataset.i = String(i);
      c.style.backgroundImage = 'url(' + JSON.stringify(rec.imageUrl || M.IMG + 'cover-fog.jpg') + ')';
      c.innerHTML = '<div class="cf-scrim"></div><p class="cf-title">' + esc(rec.title) + '</p>';
      track.appendChild(c);
    });
    updateCf();
    reveal(sec);
  }

  function cfBind() {
    $('#cfPrev').onclick = () => { if (recIdx > 0) { recIdx--; updateCf(); } };
    $('#cfNext').onclick = () => { if (recIdx < recs.length - 1) { recIdx++; updateCf(); } };
    const stage = $('#cfStage');
    stage.addEventListener('click', e => {
      const c = e.target.closest('.cf-card');
      if (!c) return;
      const i = +c.dataset.i;
      if (i === recIdx) {
        const rec = recs[i];
        if (rec.playlistId) openPlaylist(rec.playlistId);
        else toast('这个推荐位未关联歌单');
      } else { recIdx = i; updateCf(); }
    });
    let sx = null;
    stage.addEventListener('pointerdown', e => { sx = e.clientX; });
    stage.addEventListener('pointerup', e => {
      if (sx == null) return;
      const dx = e.clientX - sx; sx = null;
      if (dx < -40 && recIdx < recs.length - 1) { recIdx++; updateCf(); }
      else if (dx > 40 && recIdx > 0) { recIdx--; updateCf(); }
    });
    stage.addEventListener('keydown', e => {
      if (e.key === 'ArrowLeft') { $('#cfPrev').click(); }
      if (e.key === 'ArrowRight') { $('#cfNext').click(); }
    });
  }

  /* ================= 歌单弹窗（推荐位入口） ================= */
  function fmtTotal(tracks) {
    const sum = tracks.reduce((a, t) => a + (t.durationSec || 0), 0);
    const h = Math.floor(sum / 3600), m = Math.round((sum % 3600) / 60);
    return h ? h + ' 小时 ' + m + ' 分钟' : m + ' 分钟';
  }
  function tracksHTML(tracks) {
    return tracks.map(t =>
      '<li><b class="no num">' + t.trackNo + '</b><span class="tt">' + esc(t.title) + '</span>' +
      '<span class="dur num">' + fmtDur(t.durationSec) + '</span></li>').join('');
  }

  async function fetchDetail(id) {
    if (detailCache.has(id)) return detailCache.get(id);
    const r = await getJSON(API + '/playlists/' + id);
    if (r.code !== 0) throw new Error(r.message || '加载失败');
    detailCache.set(id, r.data);
    return r.data;
  }

  async function openPlaylist(id) {
    let d;
    try { d = await fetchDetail(id); }
    catch (e) { toast(e.message || '网络异常，请重试', false); return; }
    dlgId = d.id;
    $('#dlgCover').src = coverFor(d);
    $('#dlgTitle').textContent = d.title;
    $('#dlgTags').textContent = (d.tags || []).join(' / ') || '暂无标签';
    $('#dlgDesc').textContent = d.description || '';
    $('#dlgDesc').hidden = !d.description;
    $('#dlgTracks').innerHTML = tracksHTML(d.tracks);
    $('#dlgStat').textContent = '共 ' + d.tracks.length + ' 首 · ' + fmtTotal(d.tracks);
    $('#dlgDetail').href = M.BASE + '/playlist?id=' + d.id;
    $('#dlgFavc').innerHTML = ICON.heart(true) + ' <b class="num">' + (counts[d.id] != null ? counts[d.id] : d.favoriteCount) + '</b> 人收藏';
    paintFav($('#dlgFav'));
    $('#plDlg').showModal();
  }

  /* ================= 歌单网格 + 翻转卡片 ================= */
  function cardHTML(p, i) {
    counts[p.id] = p.favoriteCount;
    return '<article class="scene" data-id="' + p.id + '" data-reveal style="--d:' + ((i % 4) * 80 + Math.floor(i / 4) * 60) + 'ms">' +
      '<div class="card3d" tabindex="0" role="button" aria-label="查看《' + esc(p.title) + '》曲目">' +
        '<div class="face front">' +
          '<div class="cover"><img src="' + esc(coverFor(p)) + '" alt="《' + esc(p.title) + '》封面" loading="lazy">' +
            '<button class="heart-btn" data-fav="' + p.id + '" aria-label="收藏《' + esc(p.title) + '">' + ICON.heart(false) + '</button>' +
            '<span class="peek">' + ICON.play + '曲目</span></div>' +
          '<div class="glare"></div>' +
          '<div class="meta">' +
            '<h3>' + esc(p.title) + '</h3>' +
            '<p class="tags">' + esc((p.tags || []).join(' / ')) + '</p>' +
            '<div class="mrow"><span class="favc" data-favc="' + p.id + '">' + ICON.heart(true) + '<b class="num">' + p.favoriteCount + '</b> 人收藏</span></div>' +
          '</div>' +
        '</div>' +
        '<div class="face back">' +
          '<div class="bhead"><h3><a href="' + M.BASE + '/playlist?id=' + p.id + '">' + esc(p.title) + '</a></h3>' +
            '<button class="unflip" aria-label="翻回正面">' + ICON.back + '</button></div>' +
          '<ol class="tracks"><li class="t-hint">正在取出曲目单…</li></ol>' +
          '<div class="bfoot"><span class="bstat">加载中</span>' +
            '<button class="heart-btn" data-fav="' + p.id + '" aria-label="收藏">' + ICON.heart(false) + '</button></div>' +
        '</div>' +
      '</div></article>';
  }

  const skelHTML = n => Array.from({ length: n }, () =>
    '<div class="scene sk-scene"><div class="skel sk-cover"></div><div class="skel sk-l1"></div><div class="skel sk-l2"></div></div>').join('');

  function pagerHTML(d) {
    const pages = Math.max(1, Math.ceil(d.total / d.size));
    if (pages <= 1) { pager.innerHTML = ''; return; }
    pager.innerHTML =
      '<button ' + (d.page <= 1 ? 'disabled' : '') + ' data-pg="' + (d.page - 1) + '">上一页</button>' +
      '<span class="info num">第 ' + d.page + ' / ' + pages + ' 页 · 共 ' + d.total + ' 个</span>' +
      '<button ' + (d.page >= pages ? 'disabled' : '') + ' data-pg="' + (d.page + 1) + '">下一页</button>';
    pager.querySelectorAll('button[data-pg]').forEach(b => {
      b.onclick = () => { curPage = +b.dataset.pg; load(); $('#library').scrollIntoView({ behavior: M.REDUCED ? 'auto' : 'smooth' }); };
    });
  }

  async function load() {
    pager.innerHTML = '';
    grid.innerHTML = skelHTML(8);
    try {
      const url = API + '/playlists?page=' + curPage + '&size=8' + (curTag ? '&tag=' + encodeURIComponent(curTag) : '');
      const r = await getJSON(url);
      if (r.code !== 0) { grid.innerHTML = emptyHTML('加载失败：' + r.message, false); return; }
      const d = r.data;
      $('#libCount').textContent = d.total;
      if (!d.list.length) { grid.innerHTML = emptyHTML(); pager.innerHTML = ''; return; }
      grid.innerHTML = d.list.map(cardHTML).join('');
      grid.querySelectorAll('.card3d').forEach(el => tilt(el));
      reveal(grid);
      pagerHTML(d);
    } catch (e) {
      grid.innerHTML = emptyHTML('加载失败，请确认后端服务已启动', false);
    }
  }

  function emptyHTML(msg, isEmpty = true) {
    if (!isEmpty) {
      return '<div class="empty-state" style="grid-column:1/-1"><h3>' + esc(msg) + '</h3></div>';
    }
    return '<div class="empty-state" style="grid-column:1/-1">' +
      '<div class="art">' + ICON.music + '</div>' +
      '<h3>这个标签下还没有歌单</h3><p>换个标签，或者回到全部歌单看看</p>' +
      '<button class="btn btn-ghost" id="resetTag">看全部歌单</button></div>';
  }

  /* ================= 收藏 ================= */
  function paintFav(btn) {
    const id = +btn.dataset.fav;
    const on = !!favSet[id];
    if (btn.classList.contains('heart-btn')) {
      btn.classList.toggle('on', on);
      btn.innerHTML = ICON.heart(on);
      btn.classList.remove('pop');
      void btn.offsetWidth;
      btn.classList.add('pop');
    } else {
      btn.innerHTML = ICON.heart(on) + '<span>' + (on ? '已收藏' : '加入收藏') + '</span>';
    }
  }

  async function doFav(btn) {
    const id = +btn.dataset.fav;
    if (!me) {
      location.href = M.BASE + '/login.html?redirect=' + encodeURIComponent(location.pathname + location.search);
      return;
    }
    const wasFav = !!favSet[id];
    btn.disabled = true;
    try {
      const r = await toggleFav(id, wasFav);
      if (r.code === 401) {
        location.href = M.BASE + '/login.html?redirect=' + encodeURIComponent(location.pathname + location.search);
        return;
      }
      if (r.code !== 0) { toast(r.message, false); return; }
      if (wasFav) delete favSet[id]; else favSet[id] = true;
      if (counts[id] != null) counts[id] += wasFav ? -1 : 1;
      document.querySelectorAll('[data-fav="' + id + '"]').forEach(paintFav);
      document.querySelectorAll('[data-favc="' + id + '"]').forEach(el => {
        el.innerHTML = ICON.heart(true) + '<b class="num">' + counts[id] + '</b> 人收藏';
      });
      if (dlgId === id && $('#plDlg').open) {
        $('#dlgFavc').innerHTML = ICON.heart(true) + ' <b class="num">' + counts[id] + '</b> 人收藏';
      }
      toast(wasFav ? '已取消收藏' : '已加入收藏');
    } catch (e) {
      toast('网络异常，请重试', false);
    } finally {
      btn.disabled = false;
    }
  }

  /* ================= 翻转 ================= */
  async function flip(card) {
    const id = +card.closest('.scene').dataset.id;
    card.classList.remove('tilting');
    const toFlip = !card.classList.contains('flipped');
    card.classList.toggle('flipped', toFlip);
    card.dataset.flipped = toFlip ? '1' : '0';
    if (!toFlip || card.dataset.loaded) return;
    card.dataset.loaded = '1';
    try {
      const d = await fetchDetail(id);
      card.querySelector('.tracks').innerHTML = tracksHTML(d.tracks);
      card.querySelector('.bstat').textContent = '共 ' + d.tracks.length + ' 首 · ' + fmtTotal(d.tracks);
    } catch (e) {
      card.querySelector('.tracks').innerHTML = '<li class="t-hint t-err">曲目加载失败，请稍后重试</li>';
      card.querySelector('.bstat').textContent = '加载失败';
      card.dataset.loaded = '';
    }
  }

  /* ================= 标签 ================= */
  async function loadTags() {
    let tags = [];
    try {
      const r = await getJSON(API + '/tags');
      if (r.code === 0) tags = r.data || [];
    } catch (e) { /* 标签加载失败不影响主列表 */ }
    const box = $('#chips');
    const mk = (name, label) => {
      const b = document.createElement('button');
      b.className = 'chip' + (name === curTag ? ' on' : '');
      b.innerHTML = label;
      b.onclick = () => {
        curTag = name; curPage = 1;
        box.querySelectorAll('.chip').forEach(c => c.classList.remove('on'));
        b.classList.add('on');
        load();
      };
      return b;
    };
    box.appendChild(mk('', '全部'));
    tags.forEach(t => box.appendChild(mk(t.name, esc(t.name) + '<span class="cnt num">' + t.playlistCount + '</span>')));
  }

  /* ================= 事件绑定 ================= */
  const grid = $('#grid');
  const pager = $('#pager');

  grid.addEventListener('click', e => {
    const favBtn = e.target.closest('[data-fav]');
    if (favBtn) { e.stopPropagation(); doFav(favBtn); return; }
    if (e.target.closest('a')) return;
    if (e.target.closest('.unflip')) {
      const c = e.target.closest('.card3d');
      c.classList.remove('flipped');
      c.dataset.flipped = '0';
      return;
    }
    const card = e.target.closest('.card3d');
    if (card) flip(card);
  });
  grid.addEventListener('keydown', e => {
    if ((e.key === 'Enter' || e.key === ' ') && e.target.classList.contains('card3d')) {
      e.preventDefault();
      flip(e.target);
    }
  });
  grid.addEventListener('click', e => {
    if (e.target.closest('#resetTag')) {
      curTag = ''; curPage = 1;
      document.querySelectorAll('.chip').forEach((c, i) => c.classList.toggle('on', i === 0));
      load();
    }
  });

  $('#plDlg').addEventListener('click', e => {
    const favBtn = e.target.closest('#dlgFav');
    if (favBtn && dlgId) { favBtn.dataset.fav = String(dlgId); doFav(favBtn); return; }
    if (e.target.closest('.dlg-close')) $('#plDlg').close();
  });

  /* ================= 启动 ================= */
  M.navScrollWatch();
  fireflies($('#ff'));
  heroParallax();
  cfBind();
  loadMe();
  loadTags();
  load();
  loadRecs();
  reveal();
})();
