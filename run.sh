#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
RUN_DIR="$ROOT/.run"
BACKEND_DIR="$ROOT/backend"
MALL_DIR="$ROOT/apps/mall"
ADMIN_DIR="$ROOT/apps/admin"
mkdir -p "$RUN_DIR"

# SSH 隧道: DB/Redis/MinIO 只在 john-server 本机可达。
# `ssh john-server` 经 ~/.ssh/config ProxyJump(阿里云) 再到 Tailscale 节点，
# 因此本地需能连上 Jump，且 john-server 在 Tailscale 在线。
# application.yml 默认连下面这些本地端口。
SSH_HOST="${JOHN_SSH_HOST:-john-server}"
PG_TUNNEL_PORT="${JOHN_PG_TUNNEL_PORT:-15432}"
PG_REMOTE_PORT=5432
REDIS_TUNNEL_PORT="${JOHN_REDIS_TUNNEL_PORT:-16379}"
REDIS_REMOTE_PORT=6379
S3_TUNNEL_PORT="${JOHN_S3_TUNNEL_PORT:-19000}"
S3_REMOTE_PORT=19000

BACKEND_PORT=8020
MALL_PORT=3022
ADMIN_PORT=3021

# 递归杀掉整棵进程树(含子进程)。mvn spring-boot:run / npm 都会再 spawn 子进程,
# 只 kill pid 文件里的父进程会留下孤儿占端口。
_kill_tree() {
  local pid=$1
  local sig=${2:-TERM}
  [[ -z "$pid" ]] && return 0
  local child
  while read -r child; do
    [[ -n "$child" ]] && _kill_tree "$child" "$sig"
  done < <(pgrep -P "$pid" 2>/dev/null || true)
  kill "-$sig" "$pid" 2>/dev/null || true
}

_wait_gone() {
  local pid=$1
  local tries=${2:-10}
  local i
  for ((i = 0; i < tries; i++)); do
    kill -0 "$pid" 2>/dev/null || return 0
    sleep 0.3
  done
  return 1
}

# 按端口兜底:pid 文件丢失 / 孤儿进程 / 手动起过服务时也能清干净
_free_port() {
  local port=$1
  local pids
  pids=$(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null || true)
  [[ -z "$pids" ]] && return 0
  local pid
  for pid in $pids; do
    _kill_tree "$pid" TERM
  done
  sleep 0.4
  pids=$(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null || true)
  for pid in $pids; do
    _kill_tree "$pid" KILL
  done
}

_wait_port() {
  local host=$1 port=$2 tries=${3:-40} i
  for ((i = 0; i < tries; i++)); do
    if (exec 3<>"/dev/tcp/${host}/${port}") 2>/dev/null; then
      exec 3>&- 3<&- 2>/dev/null || true
      return 0
    fi
    sleep 0.25
  done
  return 1
}

_java_major() {
  local home=${1:-}
  [[ -x "${home}/bin/java" ]] || return 1
  "${home}/bin/java" -version 2>&1 | awk -F[\".] '/version/ {print ($2=="1"?$3:$2); exit}'
}

ensure_java() {
  # 已设置但不是 17 的 JAVA_HOME 不可用（本机常残留 JDK8）
  if [[ -n "${JAVA_HOME:-}" ]]; then
    local major
    major="$(_java_major "$JAVA_HOME" || true)"
    if [[ "$major" == "17" ]]; then
      return 0
    fi
    echo "WARN: JAVA_HOME=$JAVA_HOME 不是 Java 17 (got ${major:-?})，重新解析..." >&2
    unset JAVA_HOME
  fi
  if command -v jenv >/dev/null 2>&1; then
    eval "$(cd "$ROOT" && jenv init -)" 2>/dev/null || true
    local prefix
    prefix="$(cd "$ROOT" && jenv prefix 2>/dev/null || true)"
    if [[ "$(_java_major "$prefix" || true)" == "17" ]]; then
      export JAVA_HOME="$prefix"
      return 0
    fi
  fi
  for candidate in \
    /usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
    /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
  do
    if [[ "$(_java_major "$candidate" || true)" == "17" ]]; then
      export JAVA_HOME="$candidate"
      return 0
    fi
  done
  if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    local home
    home="$(/usr/libexec/java_home -v 17 2>/dev/null || true)"
    if [[ "$(_java_major "$home" || true)" == "17" ]]; then
      export JAVA_HOME="$home"
      return 0
    fi
  fi
  echo "ERROR: 未找到 Java 17。请先 brew install openjdk@17 并用 jenv add。" >&2
  exit 1
}

# 隧道依赖 SSH；SSH 依赖 Tailscale 上的 john-server（见 ~/.ssh/config HostName 100.x）
ensure_ssh_ready() {
  if command -v tailscale >/dev/null 2>&1; then
    if ! tailscale status >/dev/null 2>&1; then
      echo "ERROR: Tailscale 未运行。请先打开 Tailscale，再执行 ./run.sh" >&2
      exit 1
    fi
    if ! tailscale status 2>/dev/null | grep -qE '[[:space:]]john-server[[:space:]]'; then
      echo "WARN: tailscale status 里看不到 john-server，SSH 隧道可能会失败。" >&2
    fi
  else
    echo "WARN: 未安装 tailscale CLI，跳过在线检查（仍尝试 SSH）。" >&2
  fi

  if ! ssh -o BatchMode=yes -o ConnectTimeout=8 "$SSH_HOST" 'echo ok' >/dev/null 2>&1; then
    echo "ERROR: 无法 SSH 到 ${SSH_HOST}。" >&2
    echo "  请确认: 1) Tailscale 已连接  2) ~/.ssh/config 中 Host ${SSH_HOST} 可用  3) ProxyJump 跳板可达" >&2
    echo "  调试: ssh -v ${SSH_HOST}" >&2
    exit 1
  fi
}

start_tunnel() {
  local name=$1 localport=$2 remoteport=$3
  if lsof -tiTCP:"$localport" -sTCP:LISTEN >/dev/null 2>&1; then
    return 0
  fi
  : >"$RUN_DIR/tunnel-${name}.log"
  nohup ssh -N \
    -o ExitOnForwardFailure=yes \
    -o ServerAliveInterval=30 -o ServerAliveCountMax=3 \
    -o ConnectTimeout=10 \
    -L "127.0.0.1:${localport}:127.0.0.1:${remoteport}" "$SSH_HOST" \
    >"$RUN_DIR/tunnel-${name}.log" 2>&1 &
  echo $! >"$RUN_DIR/tunnel-${name}.pid"
  if ! _wait_port 127.0.0.1 "$localport" 40; then
    echo "ERROR: SSH 隧道 ${name} (127.0.0.1:${localport} -> ${SSH_HOST}:${remoteport}) 启动失败" >&2
    sed "s/^/  [tunnel-${name}] /" "$RUN_DIR/tunnel-${name}.log" >&2 2>/dev/null || true
    echo "  提示: 多半是 Tailscale/SSH 不通，先: ssh ${SSH_HOST}" >&2
    exit 1
  fi
}

ensure_tunnels() {
  ensure_ssh_ready
  start_tunnel pg "$PG_TUNNEL_PORT" "$PG_REMOTE_PORT"
  start_tunnel redis "$REDIS_TUNNEL_PORT" "$REDIS_REMOTE_PORT"
  start_tunnel minio "$S3_TUNNEL_PORT" "$S3_REMOTE_PORT"
  echo "SSH 隧道就绪: DB 127.0.0.1:${PG_TUNNEL_PORT}  Redis 127.0.0.1:${REDIS_TUNNEL_PORT}  MinIO 127.0.0.1:${S3_TUNNEL_PORT}  (-> ${SSH_HOST})"
}

stop_tunnels() {
  stop_svc tunnel-pg
  stop_svc tunnel-redis
  stop_svc tunnel-minio
  _free_port "$PG_TUNNEL_PORT"
  _free_port "$REDIS_TUNNEL_PORT"
  _free_port "$S3_TUNNEL_PORT"
}

stop_svc() {
  local name=$1
  local pid_file="$RUN_DIR/${name}.pid"
  if [[ -f "$pid_file" ]]; then
    local pid
    pid=$(tr -d '[:space:]' <"$pid_file")
    if [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null; then
      _kill_tree "$pid" TERM
      if ! _wait_gone "$pid" 10; then
        _kill_tree "$pid" KILL
        _wait_gone "$pid" 5 || true
      fi
    fi
    rm -f "$pid_file"
  fi
}

stop_all() {
  stop_svc mall
  stop_svc admin
  stop_svc backend
  _free_port "$MALL_PORT"
  _free_port "$ADMIN_PORT"
  _free_port "$BACKEND_PORT"
}

start_backend() {
  ensure_java
  if ! command -v mvn >/dev/null 2>&1; then
    echo "ERROR: 未找到 mvn，请先安装 Maven。" >&2
    exit 1
  fi
  cd "$BACKEND_DIR"
  _free_port "$BACKEND_PORT"
  : >"$RUN_DIR/backend.log"
  echo "JAVA_HOME=${JAVA_HOME} ($(_java_major "$JAVA_HOME"))"

  # Java 会读 macOS 系统代理；即使「忽略 localhost」，仍可能带上 socksProxyHost。
  # 本地隧道必须直连，这里强制 bypass（可被环境变量 JOHN_JAVA_TOOL_OPTIONS 覆盖）。
  local java_tool_opts="${JOHN_JAVA_TOOL_OPTIONS:--Djava.net.useSystemProxies=false -DsocksProxyHost= -Dhttp.proxyHost= -Dhttps.proxyHost= -Dhttp.nonProxyHosts=localhost|127.*|*.local|[::1] -Dhttps.nonProxyHosts=localhost|127.*|*.local|[::1] -DsocksNonProxyHosts=localhost|127.*|*.local|[::1]}"

  nohup env JAVA_HOME="$JAVA_HOME" PATH="$JAVA_HOME/bin:$PATH" \
    JAVA_TOOL_OPTIONS="$java_tool_opts" \
    mvn -q spring-boot:run -Dspring-boot.run.profiles=dev \
    >"$RUN_DIR/backend.log" 2>&1 &
  echo $! >"$RUN_DIR/backend.pid"
}

_start_npm_app() {
  local name=$1
  local dir=$2
  local port=$3
  cd "$dir"
  if [[ ! -d node_modules ]]; then
    npm install
  fi
  _free_port "$port"
  : >"$RUN_DIR/${name}.log"
  nohup npm run dev >"$RUN_DIR/${name}.log" 2>&1 &
  echo $! >"$RUN_DIR/${name}.pid"
}

start_mall() {
  _start_npm_app mall "$MALL_DIR" "$MALL_PORT"
}

start_admin() {
  _start_npm_app admin "$ADMIN_DIR" "$ADMIN_PORT"
}

_print_urls() {
  echo "john-e-commerce started:"
  echo "  Mall:   http://localhost:${MALL_PORT}"
  echo "  Admin:  http://localhost:${ADMIN_PORT}"
  echo "  API:    http://localhost:${BACKEND_PORT}/api"
  echo "  Docs:   http://localhost:${BACKEND_PORT}/api/swagger-ui.html"
  echo "  Login:  13800000000 / admin123"
  echo "  Logs:   $RUN_DIR/*.log"
}

_status_line() {
  local name=$1 port=$2
  if lsof -tiTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "  ${name}: up (:${port})"
  else
    echo "  ${name}: down (:${port})"
  fi
}

print_status() {
  echo "john-e-commerce status:"
  _status_line "tunnel-pg   " "$PG_TUNNEL_PORT"
  _status_line "tunnel-redis" "$REDIS_TUNNEL_PORT"
  _status_line "tunnel-minio" "$S3_TUNNEL_PORT"
  _status_line "backend     " "$BACKEND_PORT"
  _status_line "mall        " "$MALL_PORT"
  _status_line "admin       " "$ADMIN_PORT"
}

case "${1:-start}" in
  start)
    stop_all
    ensure_tunnels
    start_backend
    start_mall
    start_admin
    _print_urls
    ;;
  stop)
    stop_all
    stop_tunnels
    echo "Stopped."
    ;;
  restart)
    stop_all
    sleep 0.5
    ensure_tunnels
    start_backend
    start_mall
    start_admin
    _print_urls
    ;;
  backend)
    stop_svc backend
    _free_port "$BACKEND_PORT"
    ensure_tunnels
    start_backend
    echo "Backend started. Log: $RUN_DIR/backend.log"
    ;;
  mall)
    stop_svc mall
    _free_port "$MALL_PORT"
    start_mall
    echo "Mall started. http://localhost:${MALL_PORT}  Log: $RUN_DIR/mall.log"
    ;;
  admin)
    stop_svc admin
    _free_port "$ADMIN_PORT"
    start_admin
    echo "Admin started. http://localhost:${ADMIN_PORT}  Log: $RUN_DIR/admin.log"
    ;;
  status)
    print_status
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|backend|mall|admin|status}"
    exit 1
    ;;
esac
