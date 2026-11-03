<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>页面不存在 · 自然音乐歌单</title>
<meta name="robots" content="noindex">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/theme.css">
<style>
.err-wrap { min-height: 100dvh; display: grid; place-items: center; padding: 40px 24px; }
.err-box { text-align: center; }
.err-code {
  font-size: clamp(72px, 14vw, 130px); font-weight: 250; line-height: 1;
  color: var(--acc); opacity: .85; letter-spacing: .06em; margin-bottom: 10px;
}
.err-msg { font-size: 16px; color: var(--txt2); margin: 0 0 28px; }
</style>
</head>
<body>
<main class="err-wrap">
  <div class="err-box">
    <div class="err-code num"><c:out value="${code}"/></div>
    <p class="err-msg"><c:out value="${msg}"/></p>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/">回首页听听雨声</a>
  </div>
</main>
</body>
</html>
