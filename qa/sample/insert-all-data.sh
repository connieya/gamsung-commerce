#!/bin/bash

# 모든 테이블(users, brand, product, product_like) 데이터 삽입 스크립트

echo "=== 모든 샘플 데이터 삽입 시작 ==="

# 환경 변수 설정
DB_HOST=${DB_HOST:-"localhost"}
DB_PORT=${DB_PORT:-"3306"}
DB_NAME=${DB_NAME:-"loopers"}
DB_USER=${DB_USER:-"application"}
DB_PASSWORD=${DB_PASSWORD:-"application"}
SQL_FILE="all-data.sql"
SQL_FILE_PATH="results/$SQL_FILE"

# ----------------------------------------------------
# 1단계: SQL 파일 생성
# ----------------------------------------------------
echo ""
echo "1단계: generate-all-data.js를 사용하여 SQL 파일 생성"
# Node.js 스크립트 실행
node scripts/generate-all-data.js

# 스크립트가 성공적으로 실행되었는지 확인
if [ $? -ne 0 ]; then
    echo "오류: SQL 파일 생성에 실패했습니다. generate-all-data.js 스크립트 로그를 확인하세요."
    exit 1
fi

if [ ! -f "$SQL_FILE_PATH" ]; then
    echo "오류: SQL 파일이 생성되지 않았습니다: $SQL_FILE_PATH"
    exit 1
fi

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
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" --default-character-set=utf8mb4 "$DB_NAME" < "$SQL_FILE_PATH"

    # 직전 명령어(mysql)의 성공 여부 확인
    if [ $? -eq 0 ]; then
        echo "데이터 삽입 완료!"
    else
        echo "오류: MySQL 데이터 삽입에 실패했습니다. 로그를 확인하세요."
        exit 1
    fi
elif command -v docker &> /dev/null; then
    echo "Docker를 사용하여 데이터 삽입..."
    MYSQL_CONTAINER=$(docker ps --filter "ancestor=mysql:8.0" --format "{{.Names}}" | head -1)
    if [ -n "$MYSQL_CONTAINER" ]; then
        echo "MySQL 컨테이너 발견: $MYSQL_CONTAINER"
        docker exec -i "$MYSQL_CONTAINER" mysql -u "$DB_USER" -p"$DB_PASSWORD" --default-character-set=utf8mb4 "$DB_NAME" < "$SQL_FILE_PATH"

        # 직전 명령어(docker exec)의 성공 여부 확인
        if [ $? -eq 0 ]; then
            echo "데이터 삽입 완료!"
        else
            echo "오류: Docker 컨테이너 내 MySQL 데이터 삽입에 실패했습니다. 로그를 확인하세요."
            exit 1
        fi
    else
        echo "오류: 실행 중인 MySQL 컨테이너를 찾을 수 없습니다."
        exit 1
    fi
else
    echo "오류: MySQL 클라이언트나 Docker를 찾을 수 없습니다."
    exit 1
fi

echo ""
echo "=== 모든 샘플 데이터 삽입 완료 ==="
