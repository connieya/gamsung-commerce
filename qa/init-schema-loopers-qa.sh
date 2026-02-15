#!/bin/bash
# loopers_qa DB 생성 및 loopers 스키마 복사 (부하 테스트용 DB 최초 1회 설정)
# - Docker 신규 기동 시 mysql-init으로 loopers_qa는 이미 생성되어 있을 수 있음 (테이블은 없음)
# - 기존 볼륨 사용 중이면 이 스크립트로 DB 생성 + 스키마 복사

set -e

DB_HOST=${DB_HOST:-"localhost"}
DB_PORT=${DB_PORT:-"3306"}
DB_USER=${DB_USER:-"application"}
DB_PASSWORD=${DB_PASSWORD:-"application"}
ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-"root"}

echo "=== loopers_qa DB 생성 및 스키마 복사 (소스: loopers) ==="
echo "호스트: $DB_HOST:$DB_PORT"

create_and_copy() {
  # 1) DB 생성 (없으면)
  mysql -h "$DB_HOST" -P "$DB_PORT" -u root -p"$ROOT_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS loopers_qa CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
  # 2) application 유저에게 권한 부여
  mysql -h "$DB_HOST" -P "$DB_PORT" -u root -p"$ROOT_PASSWORD" -e "GRANT ALL PRIVILEGES ON loopers_qa.* TO 'application'@'%'; FLUSH PRIVILEGES;"
  # 3) 스키마만 복사 (데이터 없이)
  mysqldump -h "$DB_HOST" -P "$DB_PORT" -u root -p"$ROOT_PASSWORD" --no-data --set-gtid-purged=OFF loopers | mysql -h "$DB_HOST" -P "$DB_PORT" -u root -p"$ROOT_PASSWORD" loopers_qa
  echo "loopers_qa 스키마 복사 완료."
}

docker_copy() {
  MYSQL_CONTAINER=$(docker ps --filter "ancestor=mysql:8.0" --format "{{.Names}}" | head -1)
  if [ -z "$MYSQL_CONTAINER" ]; then
    echo "오류: 실행 중인 MySQL( mysql:8.0 ) 컨테이너를 찾을 수 없습니다."
    exit 1
  fi
  echo "MySQL 컨테이너: $MYSQL_CONTAINER"
  docker exec "$MYSQL_CONTAINER" mysql -u root -p"$ROOT_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS loopers_qa CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
  docker exec "$MYSQL_CONTAINER" mysql -u root -p"$ROOT_PASSWORD" -e "GRANT ALL PRIVILEGES ON loopers_qa.* TO 'application'@'%'; FLUSH PRIVILEGES;"
  docker exec "$MYSQL_CONTAINER" bash -c "mysqldump -u root -p'$ROOT_PASSWORD' --no-data --set-gtid-purged=OFF loopers | mysql -u root -p'$ROOT_PASSWORD' loopers_qa"
  echo "loopers_qa 스키마 복사 완료."
}

if command -v mysql &> /dev/null && command -v mysqldump &> /dev/null; then
  create_and_copy
elif command -v docker &> /dev/null; then
  docker_copy
else
  echo "오류: mysql/mysqldump 또는 docker 가 필요합니다."
  exit 1
fi

echo "=== 완료 ==="
