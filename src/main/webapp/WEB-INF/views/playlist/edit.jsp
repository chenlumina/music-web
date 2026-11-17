<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>编辑歌单 · music-web</title>
  <link rel="icon" href="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M4 20c9 1 16-5.5 16-16-8.5-1-15.5 4-16 12 0 1.4.2 2.8 0 4z' fill='%237ce3ab'/%3E%3C/svg%3E">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/theme.css">
  <style>
    .wrap { max-width: 640px; margin: 0 auto; padding: 110px 24px 80px; }
    .panel { border-radius: var(--r-panel); padding: 34px 32px 36px; }
    .panel h1 { margin: 0 0 6px; font-size: 25px; font-weight: 600; letter-spacing: .03em; }
    .panel .sub { margin: 0 0 26px; font-size: 13px; color: var(--txt3); }
    .field { margin-bottom: 18px; }
    .field .hint { margin: 7px 0 0; font-size: 12px; color: var(--txt3); }
    .actions { display: flex; gap: 12px; margin-top: 26px; }
    .alert { display: flex; align-items: center; gap: 10px; padding: 13px 16px; margin-bottom: 20px;
             border-radius: 12px; font-size: 13.5px; border: 1px solid rgba(255,143,164,.32);
             background: rgba(255,143,164,.1); color: var(--rose); }
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
      <a href="${pageContext.request.contextPath}/playlist">返回列表</a>
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
  <div class="panel glass">
    <h1>编辑歌单 #${playlist.id}</h1>
    <p class="sub">标题唯一（排除自身）；改为下架后前台 /front 立即不可见</p>

    <c:if test="${not empty error}">
      <div class="alert">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 8v5M12 16h.01"/></svg>
        <c:out value="${error}"/>
      </div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/playlist" enctype="multipart/form-data">
      <input type="hidden" name="action" value="update">
      <input type="hidden" name="id" value="${playlist.id}">
      <div class="field">
        <label for="title">歌单标题 *</label>
        <input id="title" name="title" type="text" maxlength="100" required
               value="<c:out value='${playlist.title}'/>">
        <p class="hint">最长 100 个字符，全站唯一（保留原标题不算重复）</p>
      </div>

      <div class="field">
        <label for="description">歌单描述</label>
        <textarea id="description" name="description" rows="4" maxlength="500"><c:out value="${playlist.description}"/></textarea>
        <p class="hint">最长 500 个字符</p>
      </div>

      <div class="field">
        <label for="coverUrl">封面文件名</label>
        <input id="coverUrl" name="coverUrl" type="text" maxlength="255"
               value="<c:out value='${playlist.coverUrl}'/>">
        <input type="file" name="coverFile" accept="image/jpeg,image/png,image/gif,image/webp" style="margin-top:8px;font-size:13px;color:var(--txt3)">
        <p class="hint">可上传新图（≤5MB）替换封面；或填写 assets/img 下已有文件名</p>
      </div>

      <div class="field">
        <label>标签（可多选）</label>
        <div style="display:flex;gap:10px;flex-wrap:wrap">
          <c:forEach items="${allTags}" var="t">
            <label style="display:inline-flex;align-items:center;gap:6px;font-size:13px;color:var(--txt2);padding:6px 10px;border:1px solid var(--line);border-radius:999px">
              <input type="checkbox" name="tagIds" value="${t.id}" ${not empty selectedTagIds and selectedTagIds.contains(t.id) ? 'checked' : ''}> <c:out value="${t.name}"/>
            </label>
          </c:forEach>
        </div>
        <p class="hint">用于前台按标签筛选</p>
      </div>

      <div class="field">
        <label for="status">上架状态</label>
        <select id="status" name="status">
          <option value="ONLINE" ${playlist.status ne 'OFFLINE' ? 'selected' : ''}>上架（ONLINE）</option>
          <option value="OFFLINE" ${playlist.status eq 'OFFLINE' ? 'selected' : ''}>下架（OFFLINE）</option>
        </select>
      </div>

      <div class="actions">
        <button class="btn btn-primary" type="submit">保存修改</button>
        <a class="btn btn-ghost" href="${pageContext.request.contextPath}/playlist">取消</a>
      </div>
    </form>

    <form method="post" action="${pageContext.request.contextPath}/track" style="margin-top:26px;border-top:1px solid var(--line);padding-top:20px">
      <input type="hidden" name="playlistId" value="${playlist.id}">
      <div class="field">
        <label>曲目管理（${empty tracks ? 0 : tracks.size()} 条）</label>
        <div style="display:grid;gap:8px">
          <c:forEach items="${tracks}" var="tr">
            <div style="display:flex;justify-content:space-between;align-items:center;gap:10px;border:1px solid var(--line);border-radius:10px;padding:8px 12px;font-size:13px">
              <span><c:out value="${tr.title}"/> <span style="color:var(--txt3)">· <c:out value="${tr.artist}"/> · ${tr.durationText}</span></span>
              <a class="btn btn-ghost btn-sm" href="${pageContext.request.contextPath}/track?action=delete&id=${tr.id}&playlistId=${playlist.id}">删除</a>
            </div>
          </c:forEach>
          <c:if test="${empty tracks}"><p class="hint" style="margin:0">还没有曲目，用下方表单添加</p></c:if>
        </div>
      </div>
      <div class="field">
        <label>添加曲目</label>
        <div style="display:flex;gap:8px;flex-wrap:wrap">
          <input name="trackTitle" placeholder="曲名" maxlength="200" required style="flex:2;min-width:160px">
          <input name="trackArtist" placeholder="艺术家" maxlength="100" style="flex:1;min-width:120px">
          <input name="trackDuration" type="number" min="0" placeholder="秒" style="width:90px">
          <button class="btn btn-primary btn-sm" type="submit">添加</button>
        </div>
        <p class="hint">时长填秒数，例如 185 显示为 3:05</p>
      </div>
    </form>
  </div>
</main>

</body>
</html>