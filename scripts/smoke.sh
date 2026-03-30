#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FAILED=0

pass() {
  printf '[PASS] %s\n' "$1"
}

warn() {
  printf '[WARN] %s\n' "$1"
}

fail() {
  printf '[FAIL] %s\n' "$1" >&2
  FAILED=1
}

check_file() {
  local path="$1"
  local label="$2"
  if [ -f "$path" ]; then
    pass "$label"
  else
    fail "$label"
  fi
}

check_command() {
  local command_name="$1"
  local label="$2"
  if command -v "$command_name" >/dev/null 2>&1; then
    pass "$label"
  else
    fail "$label"
  fi
}

check_optional_command() {
  local command_name="$1"
  local label="$2"
  if command -v "$command_name" >/dev/null 2>&1; then
    pass "$label"
  else
    warn "未检测到 $label"
  fi
}

print_usage() {
  cat <<'EOF'
用法：
  bash scripts/smoke.sh

检查内容：
  1. 关键仓库文件是否存在
  2. Java / Maven / Node / npm 命令是否可用
  3. MySQL / Redis CLI 是否可用（可选）
  4. 关键脚本是否通过 bash 语法校验
EOF
}

if [ "${1:-}" = "--help" ] || [ "${1:-}" = "-h" ]; then
  print_usage
  exit 0
fi

printf '[INFO] 在 %s 执行本地 smoke 检查\n' "$ROOT_DIR"

check_file "$ROOT_DIR/README.md" "根 README 已存在"
check_file "$ROOT_DIR/backend/.env.example" "后端环境模板已存在"
check_file "$ROOT_DIR/backend/src/main/resources/sql/create_table.sql" "数据库建表脚本已存在"
check_file "$ROOT_DIR/frontend/package.json" "前端 package.json 已存在"
check_file "$ROOT_DIR/scripts/prepare_local.sh" "本地准备脚本已存在"
check_file "$ROOT_DIR/scripts/build_check.sh" "构建验证脚本已存在"

check_command java "Java 命令可用"
check_command mvn "Maven 命令可用"
check_command node "Node.js 命令可用"
check_command npm "npm 命令可用"
check_optional_command mysql "MySQL CLI（可选）"
check_optional_command redis-cli "Redis CLI（可选）"

if bash -n \
  "$ROOT_DIR/scripts/prepare_local.sh" \
  "$ROOT_DIR/scripts/smoke.sh" \
  "$ROOT_DIR/scripts/build_check.sh"; then
  pass "关键脚本通过 bash 语法校验"
else
  fail "关键脚本未通过 bash 语法校验"
fi

if [ "$FAILED" -ne 0 ]; then
  printf '[RESULT] smoke 检查失败，请先修复上面的失败项\n' >&2
  exit 1
fi

printf '[RESULT] smoke 检查通过\n'
