#!/bin/bash
# insert-weekly.sh
# product_metrics_weekly 더미 데이터 대량 삽입 스크립트
# TRUNCATE=1 ./insert-weekly.sh


set -e

echo "=== weekly metrics 샘플 데이터 삽입 시작 ==="

# 환경 변수 (필요 시 override)
DB_HOST=${DB_HOST:-"localhost"}
DB_PORT=${DB_PORT:-"3306"}
DB_NAME=${DB_NAME:-"loopers"}
DB_USER=${DB_USER:-"application"}
DB_PASSWORD=${DB_PASSWORD:-"application"}

# 생성 파라미터 (원하면 바꿔서 실행)
# 최근 12주간, 상품 50,000개 기준 예시
WEEKS=${WEEKS:-12}            # 몇 주치 생성할지
PRODUCTS=${PRODUCTS:-50000}   # 상품 수 (product_id 1..PRODUCTS)
SEED=${SEED:-42}              # 랜덤 시드 (재현성)
START_MONDAY=${START_MONDAY:-""}  # 특정 시작 월요일 (YYYY-MM-DD), 비우면 "이번 주 월요일부터 거슬러감"
BATCH_SIZE=${BATCH_SIZE:-2000}    # INSERT values 배치 크기 (성능)

echo ""
echo "1단계: Node 생성기로 SQL 파일 생성"
node scripts/generate-weekly-metric.js \
  --weeks "$WEEKS" \
  --products "$PRODUCTS" \
  --seed "$SEED" \
  --batch "$BATCH_SIZE" \
  ${START_MONDAY:+--start "$START_MONDAY"}

echo ""
echo "2단계: 생성된 SQL 파일을 DB에 삽입"
SQL_FILE="results/weekly-metrics.sql"
echo "DB: $DB_HOST:$DB_PORT/$DB_NAME, USER: $DB_USER"
if [ ! -f "$SQL_FILE" ]; then
  echo "오류: $SQL_FILE 을(를) 찾을 수 없습니다."; exit 1
fi

if command -v mysql &> /dev/null; then
  echo "MySQL 클라이언트로 삽입 중..."
  mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" \
    --default-character-set=utf8mb4 "$DB_NAME" < "$SQL_FILE"
  echo "데이터 삽입 완료!"
elif command -v docker &> /dev/null; then
  echo "Docker 경유로 삽입 중..."
  MYSQL_CONTAINER=$(docker ps --filter "ancestor=mysql:8.0" --format "{{.Names}}" | head -1)
  if [ -n "$MYSQL_CONTAINER" ]; then
    docker exec -i "$MYSQL_CONTAINER" \
      mysql -u "$DB_USER" -p"$DB_PASSWORD" --default-character-set=utf8mb4 "$DB_NAME" < "$SQL_FILE"
    echo "데이터 삽입 완료!"
  else
    echo "오류: 실행 중인 mysql:8.0 컨테이너를 찾지 못했습니다."; exit 1
  fi
else
  echo "오류: mysql 클라이언트나 docker 가 필요합니다."; exit 1
fi

echo ""
echo "=== weekly metrics 샘플 데이터 삽입 끝 ==="
