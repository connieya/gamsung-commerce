#!/usr/bin/env bash
# 인프라(MySQL, Redis 등)만 기동. 3306 포트 충돌 시 기존 프로세스 종료 후 docker-compose up.
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# 3306 사용 중이면 종료 (docker-compose up 전에 포트 확보)
PID=$(lsof -nP -iTCP:3306 -sTCP:LISTEN -t 2>/dev/null || true)
if [ -n "$PID" ]; then
  echo "Port 3306 in use (PID $PID), killing..."
  kill "$PID" 2>/dev/null || sudo kill "$PID"
  sleep 2
fi

docker-compose -f docker/infra-compose.yml up -d
echo "Waiting for MySQL to be ready..."
sleep 5
