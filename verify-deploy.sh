#!/usr/bin/env bash
# music-web 部署后验证脚本：断言关键页面/接口可用，任一失败退出码 1。
# 用法：bash verify-deploy.sh [http://localhost:8080/music-web]
set -u

BASE=${1:-http://localhost:8080/music-web}
fail=0

check() {
  local name=$1 url=$2 expect=$3 code
  code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 "$url" || echo 000)
  echo "$name -> $code (expect $expect)"
  [ "$code" = "$expect" ] || fail=1
}

check "首页       " "$BASE/" 200
check "登录页     " "$BASE/login.html" 200
check "歌单详情   " "$BASE/playlist?id=1" 200
check "列表API    " "$BASE/api/playlists" 200
check "标签API    " "$BASE/api/tags" 200
check "sitemap    " "$BASE/sitemap.xml" 200

# 后台接口未登录必须是 HTTP 200 + body code=401（项目统一错误结构）
admin_body=$(curl -s --max-time 10 "$BASE/api/admin/tags" || true)
if echo "$admin_body" | grep -q '"code":401'; then
  echo "后台门禁   -> code=401 (expect 401)"
else
  echo "后台门禁   -> 未返回 code=401"
  fail=1
fi

if [ "$fail" -eq 0 ]; then
  echo "DEPLOY OK"
  exit 0
fi
echo "DEPLOY FAIL"
exit 1