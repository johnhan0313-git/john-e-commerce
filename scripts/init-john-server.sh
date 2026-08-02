#!/usr/bin/env bash
# 在 john-server 上创建 john-ecommerce 库用户/库，并按序执行 scripts/sql/V*.sql
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
HOST="${JOHN_SERVER_HOST:-john-server}"
PG_CONTAINER="${PG_CONTAINER:-john-postgresql}"
PG_ADMIN_USER="${PG_ADMIN_USER:-appuser}"
PG_ADMIN_DB="${PG_ADMIN_DB:-appdb}"
DB_USER="john-ecommerce"
DB_PASS="john-ecommerce-123"
DB_NAME="john-ecommerce"
DB_NAME_TEST="john-ecommerce-test"

echo "==> Creating PostgreSQL user/databases on ${HOST}..."

ssh "${HOST}" bash -s <<REMOTE
set -euo pipefail
PG_CONTAINER="${PG_CONTAINER}"
PG_ADMIN_USER="${PG_ADMIN_USER}"
PG_ADMIN_DB="${PG_ADMIN_DB}"
DB_USER="${DB_USER}"
DB_PASS="${DB_PASS}"
DB_NAME="${DB_NAME}"
DB_NAME_TEST="${DB_NAME_TEST}"

psql_cmd() {
  docker exec "\${PG_CONTAINER}" psql -U "\${PG_ADMIN_USER}" -d "\${PG_ADMIN_DB}" -c "\$1"
}

psql_cmd "SELECT 1 FROM pg_roles WHERE rolname = '\${DB_USER}'" | grep -q 1 || \
  psql_cmd "CREATE USER \"\${DB_USER}\" WITH PASSWORD '\${DB_PASS}';"

psql_cmd "SELECT 1 FROM pg_database WHERE datname = '\${DB_NAME}'" | grep -q 1 || \
  psql_cmd "CREATE DATABASE \"\${DB_NAME}\" OWNER \"\${DB_USER}\";"

psql_cmd "SELECT 1 FROM pg_database WHERE datname = '\${DB_NAME_TEST}'" | grep -q 1 || \
  psql_cmd "CREATE DATABASE \"\${DB_NAME_TEST}\" OWNER \"\${DB_USER}\";"

echo "PostgreSQL user + databases ready."
REMOTE

apply_migrations() {
  local db="$1"
  echo "==> Applying SQL migrations to ${db}..."
  for f in "$ROOT"/scripts/sql/V*.sql; do
    echo "  -> ${db}: $(basename "$f")"
    # 通过 stdin 喂给容器内 psql，避免 scp
    ssh "${HOST}" "docker exec -i ${PG_CONTAINER} psql -U ${DB_USER} -d ${db} -v ON_ERROR_STOP=1" <"$f"
  done
}

apply_migrations "${DB_NAME}"
apply_migrations "${DB_NAME_TEST}"

echo "==> Done."
echo "    DB:       ${DB_NAME} / ${DB_NAME_TEST}"
echo "    User:     ${DB_USER} / ${DB_PASS}"
echo "    Admin:    email johnhan0313@gmail.com (邮箱验证码登录)"
echo "    Redis DB: 6 (see application.yml)"
echo "    MinIO:    bucket john-ecommerce"
