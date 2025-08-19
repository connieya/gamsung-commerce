#!/bin/bash

# 사용자 데이터 생성 및 삽입 스크립트

echo "=== 사용자 샘플 데이터 삽입 시작 ==="

# 환경 변수 설정
DB_HOST=${DB_HOST:-"localhost"}
DB_PORT=${DB_PORT:-"3306"}
DB_NAME=${DB_NAME:-"loopers"}
DB_USER=${DB_USER:-"application"}
DB_PASSWORD=${DB_PASSWORD:-"application"}

# ----------------------------------------------------
# 1단계: SQL 파일 생성
# ----------------------------------------------------
echo ""
echo "1단계: generate-users.js를 사용하여 SQL 파일 생성"
# Node.js 스크립트 실행
node scripts/generate-users.js

# ----------------------------------------------------
# 2단계: 데이터베이스에 삽입
# ----------------------------------------------------
echo ""
echo "2단계: 생성된 SQL 파일 데이터베이스에 삽입"
echo "데이터베이스: $DB_HOST:$DB_PORT/$DB_NAME"
echo "사용자: $DB_USER"

# MySQL 클라이언트 또는 Docker를 사용하여 SQL 파일 실행
if command -v mysql &> /dev/null; then
    echo "MySQL 클라이언트를 사용하여 데이터 삽입..."
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" --default-character-set=utf8mb4 "$DB_NAME" < results/users-data.sql
    echo "데이터 삽입 완료!"
elif command -v docker &> /dev/null; then
    echo "Docker를 사용하여 데이터 삽입..."
    MYSQL_CONTAINER=$(docker ps --filter "ancestor=mysql:8.0" --format "{{.Names}}" | head -1)
    if [ -n "$MYSQL_CONTAINER" ]; then
        echo "MySQL 컨테이너 발견: $MYSQL_CONTAINER"
        docker exec -i "$MYSQL_CONTAINER" mysql -u "$DB_USER" -p"$DB_PASSWORD" --default-character-set=utf8mb4 "$DB_NAME" < results/users-data.sql
        echo "데이터 삽입 완료!"
    else
        echo "오류: 실행 중인 MySQL 컨테이너를 찾을 수 없습니다."
        exit 1
    fi
else
    echo "오류: MySQL 클라이언트나 Docker를 찾을 수 없습니다."
    exit 1
fi

echo ""
echo "=== 사용자 샘플 데이터 삽입 완료 ==="
