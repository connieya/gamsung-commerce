#!/usr/bin/env bash
# 인프라 기동 후 commerce-api Spring Boot 실행 (한 번에).
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# 3306 사용 중이면 종료
PID=$(lsof -nP -iTCP:3306 -sTCP:LISTEN -t 2>/dev/null || true)
if [ -n "$PID" ]; then
  echo "Port 3306 in use (PID $PID), killing..."
  kill "$PID" 2>/dev/null || sudo kill "$PID"
  sleep 2
fi

docker-compose -f docker/infra-compose.yml up -d
echo "Waiting for MySQL to be ready..."
sleep 5

./gradlew :apps:commerce-api:bootRun
