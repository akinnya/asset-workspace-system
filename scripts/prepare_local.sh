#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
ENV_EXAMPLE="$BACKEND_DIR/.env.example"
ENV_FILE="$BACKEND_DIR/.env"
UPLOAD_DIR="$BACKEND_DIR/data/uploads"

echo "[asset-workspace-system] 准备本地开发文件"

if [ ! -f "$ENV_EXAMPLE" ]; then
  echo "未找到 $ENV_EXAMPLE" >&2
  exit 1
fi

if [ ! -f "$ENV_FILE" ]; then
  cp "$ENV_EXAMPLE" "$ENV_FILE"
  echo "已创建 backend/.env"
else
  echo "backend/.env 已存在，跳过复制"
fi

mkdir -p "$UPLOAD_DIR"
echo "已确保本地存储目录存在: $UPLOAD_DIR"

cat <<'EOF'
下一步建议：
1. 创建数据库 `asset_workspace_system`
2. 导入 `backend/src/main/resources/sql/create_table.sql`
3. 启动 Redis
4. 启动 backend 与 frontend
EOF
