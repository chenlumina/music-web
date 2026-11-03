/* ============================================================
   自然音乐歌单 · 前端共享工具
   依赖：页面先设置 window.APP_BASE，再引入本文件
   ============================================================ */
window.MUSIC = (function () {
  'use strict';
  const BASE = (function () {
    if (window.APP_BASE) return String(window.APP_BASE).replace(/\/+$/, '');
    // 未显式注入时从当前路径推导 context（兼容 /music-web/、IDEA 的 /music_web_war_exploded/ 与根部署）
    const seg = location.pathname.split('/')[1] || '';
    return seg && !seg.includes('.') ? '/' + seg : '';
  })();
  const API = BASE + '/api';
  const IMG = BASE + '/assets/img/';
  const REDUCED = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  /* ---------- 基础 ---------- */
  // 同时转义引号：esc 的结果会拼进 HTML 属性，只转义 &<> 会破坏属性结构
  const esc = s => String(s == null ? '' : s).replace(/[&<>"']/g, c => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[c]));
  const fmtDur = sec => {
    sec = Math.max(0, sec | 0);
    const m = Math.floor(sec / 60), s = sec % 60;
    return m + ':' + String(s).padStart(2, '0');
  };

  async function getJSON(url, opt) {
    const r = await fetch(url, opt);
    return r.json();
  }

  /* ---------- Toast ---------- */
  function toastZone() {
    let z = document.getElementById('toast-zone');
    if (!z) { z = document.createElement('div'); z.id = 'toast-zone'; document.body.appendChild(z); }
    return z;
  }
  function toast(msg, ok = true) {
    const el = document.createElement('div');
    el.className = 'toast ' + (ok ? 'ok' : 'err');
    el.innerHTML = (ok ? ICON.check : ICON.alert) + '<span>' + esc(msg) + '</span>';
    toastZone().appendChild(el);
    setTimeout(() => { el.classList.add('bye'); setTimeout(() => el.remove(), 420); }, 2600);
  }

  /* ---------- 图标（内联 SVG，统一 1.6 描边） ---------- */
  const S = 'fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"';
  const ICON = {
    leaf: '<svg viewBox="0 0 24 24" ' + S + '><path d="M4 20c9 1 16-5.5 16-16-8.5-1-15.5 4-16 12 0 1.4.2 2.8 0 4z"/><path d="M4 20C7 14 11 10.5 15.5 8.5"/></svg>',
    heart: f => '<svg viewBox="0 0 24 24" ' + S + ' ' + (f ? 'fill="currentColor" ' : '') + '><path d="M12 20.3 4.9 13a4.7 4.7 0 0 1 0-6.6 4.5 4.5 0 0 1 6.5 0l.6.7.6-.7a4.5 4.5 0 0 1 6.5 0 4.7 4.7 0 0 1 0 6.6z"/></svg>',
    play: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M8.5 5.6a1 1 0 0 1 1.5-.87l9 5.4a1 1 0 0 1 0 1.73l-9 5.4a1 1 0 0 1-1.5-.86z"/></svg>',
    pause: '<svg viewBox="0 0 24 24" fill="currentColor"><rect x="6.5" y="5" width="3.6" height="14" rx="1.4"/><rect x="13.9" y="5" width="3.6" height="14" rx="1.4"/></svg>',
    music: '<svg viewBox="0 0 24 24" ' + S + '><circle cx="7" cy="17.5" r="2.6"/><circle cx="17.5" cy="15.5" r="2.6"/><path d="M9.6 17.5V7.2a1 1 0 0 1 .8-1l7-1.5a1 1 0 0 1 1.1 1v9.8"/></svg>',
    check: '<svg viewBox="0 0 24 24" ' + S + '><path d="m4.5 12.5 5 5 10-11"/></svg>',
    alert: '<svg viewBox="0 0 24 24" ' + S + '><circle cx="12" cy="12" r="9"/><path d="M12 7.5v5.5"/><circle cx="12" cy="16.6" r=".4" fill="currentColor"/></svg>',
    close: '<svg viewBox="0 0 24 24" ' + S + '><path d="m6 6 12 12M18 6 6 18"/></svg>',
    back: '<svg viewBox="0 0 24 24" ' + S + '><path d="M14.5 5.5 8 12l6.5 6.5"/></svg>',
    chevL: '<svg viewBox="0 0 24 24" ' + S + '><path d="M14.5 5.5 8 12l6.5 6.5"/></svg>',
    chevR: '<svg viewBox="0 0 24 24" ' + S + '><path d="m9.5 5.5 6.5 6.5-6.5 6.5"/></svg>',
    spark: '<svg viewBox="0 0 24 24" ' + S + '><path d="M12 3v4M12 17v4M3 12h4M17 12h4M5.6 5.6l2.8 2.8M15.6 15.6l2.8 2.8M18.4 5.6l-2.8 2.8M8.4 15.6l-2.8 2.8"/></svg>',
    eye: '<svg viewBox="0 0 24 24" ' + S + '><path d="M2.5 12S6 5.8 12 5.8 21.5 12 21.5 12 18 18.2 12 18.2 2.5 12 2.5 12z"/><circle cx="12" cy="12" r="2.8"/></svg>',
    eyeOff: '<svg viewBox="0 0 24 24" ' + S + '><path d="M4 4.5 20 19.5M9.9 6.3A8.8 8.8 0 0 1 12 5.8c6 0 9.5 6.2 9.5 6.2a17 17 0 0 1-2.8 3.5M6 8.3A16.6 16.6 0 0 0 2.5 12S6 18.2 12 18.2a9 9 0 0 0 3.2-.6"/><path d="M9.9 9.7a2.9 2.9 0 0 0 4.1 4.1"/></svg>'
  };

  /* ---------- 封面：优先真实封面，否则按标签映射内置素材 ---------- */
  const TAG_COVER = {
    '雨声': 'cover-rain.jpg', '溪流': 'cover-stream.jpg', '鸟鸣': 'cover-bird.jpg',
    '森林': 'cover-forest.jpg', '海浪': 'cover-waves.jpg', '篝火': 'cover-campfire.jpg',
    '白噪音': 'cover-fog.jpg', '夜晚': 'cover-night.jpg'
  };
  function coverFor(p) {
    if (p && p.coverUrl) return p.coverUrl;
    const t = ((p && p.tags) || []).map(x => TAG_COVER[x]).find(Boolean);
    return IMG + (t || 'cover-fog.jpg');
  }

  /* ---------- 滚动入场 ---------- */
  function reveal(root) {
    const els = (root || document).querySelectorAll('[data-reveal]:not(.in)');
    if (REDUCED || !('IntersectionObserver' in window)) { els.forEach(e => e.classList.add('in')); return; }
    const io = new IntersectionObserver(entries => {
      entries.forEach(en => {
        if (en.isIntersecting) { en.target.classList.add('in'); io.unobserve(en.target); }
      });
    }, { threshold: .1, rootMargin: '0px 0px -6% 0px' });
    els.forEach(e => io.observe(e));
  }

  /* ---------- 导航滚动态（哨兵法，不用 scroll 监听） ---------- */
  function navScrollWatch() {
    const nav = document.querySelector('.nav');
    const sentinel = document.createElement('div');
    document.body.prepend(sentinel);
    new IntersectionObserver(([en]) => {
      nav.classList.toggle('scrolled', !en.isIntersecting);
    }).observe(sentinel);
  }

  /* ---------- 3D 倾斜跟随（仅精确指针设备） ---------- */
  function tilt(el, max = 7) {
    if (REDUCED || !window.matchMedia('(pointer: fine)').matches) return;
    let raf = 0;
    el.addEventListener('pointermove', e => {
      if (el.dataset.flipped === '1') return;
      el.classList.add('tilting');
      const r = el.getBoundingClientRect();
      const px = (e.clientX - r.left) / r.width - .5;
      const py = (e.clientY - r.top) / r.height - .5;
      cancelAnimationFrame(raf);
      raf = requestAnimationFrame(() => {
        el.style.setProperty('--ry', (px * max).toFixed(2) + 'deg');
        el.style.setProperty('--rx', (-py * max).toFixed(2) + 'deg');
        el.style.setProperty('--gx', ((px + .5) * 100).toFixed(1) + '%');
        el.style.setProperty('--gy', ((py + .5) * 100).toFixed(1) + '%');
      });
    });
    el.addEventListener('pointerleave', () => {
      cancelAnimationFrame(raf);
      el.classList.remove('tilting');
      el.style.setProperty('--ry', '0deg');
      el.style.setProperty('--rx', '0deg');
    });
  }

  /* ---------- 萤火虫粒子：z 轴投影 + 鼠标视差 ---------- */
  function fireflies(canvas, count = 36) {
    if (REDUCED) return;
    const ctx = canvas.getContext('2d');
    let W, H, dpr = Math.min(window.devicePixelRatio || 1, 2);
    let mx = 0, my = 0;
    const P = [];
    function resize() {
      W = canvas.clientWidth; H = canvas.clientHeight;
      canvas.width = W * dpr; canvas.height = H * dpr;
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    }
    function spawn(z) {
      return {
        x: (Math.random() - .5) * 2.2, y: (Math.random() - .5) * 1.6,
        z: z ?? (Math.random() * .92 + .08),
        r: Math.random() * Math.PI * 2,
        warm: Math.random() < .78
      };
    }
    for (let i = 0; i < count; i++) P.push(spawn());
    resize();
    new ResizeObserver(resize).observe(canvas);
    window.addEventListener('pointermove', e => {
      mx = e.clientX / window.innerWidth - .5;
      my = e.clientY / window.innerHeight - .5;
    }, { passive: true });
    let t = 0;
    (function frame() {
      t += .016;
      ctx.clearRect(0, 0, W, H);
      for (const p of P) {
        p.z -= .0006;                 // 缓慢向观者漂近
        p.r += .008 + p.z * .01;
        if (p.z <= .04) Object.assign(p, spawn(1));
        const sc = 1 / p.z;           // 透视投影
        const drift = Math.sin(p.r) * 26 * sc;
        const x = (p.x * W * sc) + W / 2 + drift - mx * 34 * sc;
        const y = (p.y * H * sc) + H / 2 + Math.cos(p.r * .8) * 16 * sc - my * 22 * sc;
        const rad = Math.max(.6, 1.7 * sc);
        const glow = (Math.sin(t * 1.4 + p.r * 5) + 1) / 2;   // 呼吸明灭
        const alpha = (.12 + glow * .5) * Math.min(1, p.z * 1.6);
        ctx.beginPath();
        ctx.fillStyle = p.warm ? 'rgba(242,192,120,' + alpha + ')' : 'rgba(150,235,190,' + alpha + ')';
        ctx.arc(x, y, rad, 0, 7);
        ctx.fill();
      }
      requestAnimationFrame(frame);
    })();
  }

  /* ---------- 收藏切换（返回接口结果，UI 由调用方更新） ---------- */
  async function toggleFav(id, wasFav) {
    const opt = wasFav
      ? { method: 'DELETE' }
      : { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ playlistId: id }) };
    return getJSON(API + '/favorites/' + (wasFav ? id : ''), opt);
  }

  return { BASE, API, IMG, REDUCED, esc, fmtDur, getJSON, toast, ICON, coverFor, reveal, navScrollWatch, tilt, fireflies, toggleFav };
})();
