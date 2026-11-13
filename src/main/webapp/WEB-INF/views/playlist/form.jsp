<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>新增歌单 · music-lab</title>
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
    <h1>新增歌单</h1>
    <p class="sub">标题必填且不可与已有歌单重复，状态只允许上架或下架</p>

    <c:if test="${not empty error}">
      <div class="alert">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 8v5M12 16h.01"/></svg>
        <c:out value="${error}"/>
      </div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/playlist">
      <div class="field">
        <label for="title">歌单标题 *</label>
        <input id="title" name="title" type="text" maxlength="100" required
               placeholder="例如：雨夜书房 · 沉浸白噪音"
               value="<c:out value='${formTitle}'/>">
        <p class="hint">最长 100 个字符，全站唯一</p>
      </div>

      <div class="field">
        <label for="description">歌单描述</label>
        <textarea id="description" name="description" rows="4" maxlength="500"
                  placeholder="简单说明这份歌单适合什么场景"><c:out value="${formDescription}"/></textarea>
        <p class="hint">最长 500 个字符</p>
      </div>

      <div class="field">
        <label for="coverUrl">封面文件名</label>
        <input id="coverUrl" name="coverUrl" type="text" maxlength="255"
               placeholder="cover-rain.jpg"
               value="<c:out value='${formCoverUrl}'/>">
        <p class="hint">使用 assets/img 下已有图片，例如 cover-rain.jpg、cover-forest.jpg</p>
      </div>

      <div class="field">
        <label for="status">上架状态</label>
        <select id="status" name="status">
          <option value="ONLINE" ${formStatus ne 'OFFLINE' ? 'selected' : ''}>上架（ONLINE）</option>
          <option value="OFFLINE" ${formStatus eq 'OFFLINE' ? 'selected' : ''}>下架（OFFLINE）</option>
        </select>
      </div>

      <div class="actions">
        <button class="btn btn-primary" type="submit">提交</button>
        <a class="btn btn-ghost" href="${pageContext.request.contextPath}/playlist">取消</a>
      </div>
    </form>
  </div>
</main>

</body>
</html>