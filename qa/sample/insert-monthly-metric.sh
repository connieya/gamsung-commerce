#!/bin/bash
# insert-monthly.sh
# product_metrics_monthly 더미 데이터 대량 삽입 스크립트
# 예)
#   TRUNCATE=1 ./insert-monthly.sh
#   REPLACE=1 MONTHS=6 PRODUCTS=20000 ./insert-monthly.sh
#   START_MONTH=2025-07-01 ./insert-monthly.sh

set -e

echo "=== monthly metrics 샘플 데이터 삽입 시작 ==="

# DB 환경 변수
DB_HOST=${DB_HOST:-"localhost"}
DB_PORT=${DB_PORT:-"3306"}
DB_NAME=${DB_NAME:-"loopers"}
DB_USER=${DB_USER:-"application"}
DB_PASSWORD=${DB_PASSWORD:-"application"}

# 생성 파라미터
MONTHS=${MONTHS:-12}            # 최근 N개월
PRODUCTS=${PRODUCTS:-50000}     # 상품 수 (1..PRODUCTS)
SEED=${SEED:-42}                # 랜덤 시드
START_MONTH=${START_MONTH:-""}  # 특정 시작 월(YYYY-MM-01). 비우면 "이번 달 1일"부터 과거로
BATCH_SIZE=${BATCH_SIZE:-2000}  # INSERT values 배치 크기

# 재실행 옵션
TRUNCATE=${TRUNCATE:-0}   # 1이면 테이블 비우고 시작
REPLACE=${REPLACE:-0}     # 1이면 (month_start, ref_product_id) 충돌 시 UPSERT

echo ""
echo "1단계: Node 생성기로 SQL 파일 생성"
node scripts/generate-monthly-metric.js \
  --months "$MONTHS" \
  --products "$PRODUCTS" \
  --seed "$SEED" \
  --batch "$BATCH_SIZE" \
  ${START_MONTH:+--start "$START_MONTH"} \
  ${TRUNCATE:+--truncate "$TRUNCATE"} \
  ${REPLACE:+--replace "$REPLACE"}

echo ""
echo "2단계: 생성된 SQL 파일 DB에 삽입"
SQL_FILE="results/monthly-metrics.sql"
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
echo "=== monthly metrics 샘플 데이터 삽입 끝 ==="
