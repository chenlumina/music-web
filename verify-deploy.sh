#!/usr/bin/env bash
# 实验七部署后验证脚本：探活 + 登录页可达，全通过返回 0
# 用法：bash verify-deploy.sh [BASE]，默认 http://localhost:9531/music-web
BASE=${1:-http://localhost:9531/music-web}
fail=0
check() {
  local name=$1 url=$2 expect=$3
  local code
  code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 "$url")
  echo "$name -> $code (expect $expect)"
  [ "$code" = "$expect" ] || fail=1
}
check "health" "$BASE/health" 200
check "login " "$BASE/login" 200
check "front " "$BASE/front" 200
if [ $fail -eq 0 ]; then echo "DEPLOY OK"; exit 0; else echo "DEPLOY FAIL"; exit 1; fi