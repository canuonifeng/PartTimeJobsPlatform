#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
CACHE_DIR="$ROOT_DIR/.deploy-md5"
LOG_DIR="$ROOT_DIR/logs"
mkdir -p "$CACHE_DIR" "$LOG_DIR"

md5_text() {
  if command -v md5sum >/dev/null 2>&1; then
    md5sum | awk '{print $1}'
  else
    md5 -q
  fi
}

dir_md5() {
  local dir="$1"
  (
    cd "$ROOT_DIR/$dir"
    find . -type f \
      ! -path './target/*' \
      ! -path './dist/*' \
      ! -path './node_modules/*' \
      ! -path './.git/*' \
      ! -path './.idea/*' \
      ! -path './.vscode/*' \
      ! -name '.DS_Store' \
      -print \
      | LC_ALL=C sort \
      | while IFS= read -r file; do
          if command -v md5sum >/dev/null 2>&1; then
            md5sum "$file"
          else
            md5 -r "$file"
          fi
        done
  ) | md5_text
}

has_changed() {
  local name="$1"
  local dir="$2"
  local cache_file="$CACHE_DIR/$name.md5"
  local current_md5
  current_md5="$(dir_md5 "$dir")"

  if [[ ! -f "$cache_file" ]] || [[ "$(cat "$cache_file")" != "$current_md5" ]]; then
    echo "$current_md5" > "$cache_file.next"
    return 0
  fi
  return 1
}

mark_done() {
  local name="$1"
  local next_file="$CACHE_DIR/$name.md5.next"
  if [[ -f "$next_file" ]]; then
    mv "$next_file" "$CACHE_DIR/$name.md5"
  fi
}

kill_port() {
  local port="$1"
  local pids
  pids="$(lsof -ti:"$port" 2>/dev/null || true)"
  if [[ -n "$pids" ]]; then
    echo "Stopping service on port $port"
    kill -9 $pids 2>/dev/null || true
  fi
}

is_port_running() {
  local port="$1"
  [[ -n "$(lsof -ti:"$port" 2>/dev/null || true)" ]]
}

start_backend() {
  local name="$1"
  local jar="$2"
  local config="$3"

  (
    cd "$ROOT_DIR/$name/target"
    nohup java -jar "$jar" --spring.profiles.active=prod --spring.config.additional-location="optional:file:../../config/$config" > "$LOG_DIR/$name.log" 2>&1 &
  )
}

build_and_restart_backend() {
  local name="$1"
  local port="$2"
  local jar="$3"
  local config="$4"
  local jar_path="$ROOT_DIR/$name/target/$jar"

  if has_changed "$name" "$name" || [[ ! -f "$jar_path" ]]; then
    echo "[$name] changes detected or jar missing, rebuilding"
    (
      cd "$ROOT_DIR/$name"
      mvn clean package -Dmaven.test.skip=true
    )
    kill_port "$port"
    start_backend "$name" "$jar" "$config"
    mark_done "$name"
  elif ! is_port_running "$port"; then
    echo "[$name] no changes, service not running, starting existing jar"
    start_backend "$name" "$jar" "$config"
  else
    echo "[$name] no changes and service running, skip build and restart"
  fi
}

build_frontend() {
  local name="$1"
  local dist_dir="$ROOT_DIR/$name/dist"

  if has_changed "$name" "$name" || [[ ! -d "$dist_dir" ]]; then
    echo "[$name] changes detected or dist missing, rebuilding"
    (
      cd "$ROOT_DIR/$name"
      npm install
      npm run build
    )
    mark_done "$name"
  else
    echo "[$name] no changes, skip build"
  fi
}

build_and_restart_backend "c-service" "8082" "c-service-1.0.0-SNAPSHOT.jar" "c-service-env.yml"
build_and_restart_backend "enterprise-service" "8081" "enterprise-service-1.0.0-SNAPSHOT.jar" "enterprise-service-env.yml"
build_and_restart_backend "platform-service" "8083" "platform-service-1.0.0-SNAPSHOT.jar" "platform-service-env.yml"

build_frontend "enterprise-pc"
build_frontend "platform-pc"
