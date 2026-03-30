#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"

print_usage() {
  cat <<'EOF'
用法：
  bash scripts/build_check.sh [backend|frontend|all]

说明：
  backend  仅执行后端编译检查
  frontend 仅执行前端构建检查
  all      依次执行后端编译和前端构建
EOF
}

run_backend() {
  printf '[INFO] 执行后端编译检查\n'
  (
    cd "$BACKEND_DIR"
    mvn -q -DskipTests compile
  )
}

run_frontend() {
  printf '[INFO] 执行前端构建检查\n'
  (
    cd "$FRONTEND_DIR"
    npm run build
  )
}

MODE="${1:-all}"

case "$MODE" in
  -h|--help)
    print_usage
    exit 0
    ;;
  backend)
    run_backend
    ;;
  frontend)
    run_frontend
    ;;
  all)
    run_backend
    run_frontend
    ;;
  *)
    print_usage >&2
    exit 1
    ;;
esac

printf '[RESULT] build 检查完成\n'
