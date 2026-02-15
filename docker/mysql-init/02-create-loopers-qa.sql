-- 부하 테스트·QA 전용 DB (로컬 화면용 loopers와 분리)
-- docker-entrypoint-initdb.d 에서 컨테이너 최초 기동 시 한 번만 실행됨
CREATE DATABASE IF NOT EXISTS loopers_qa
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

-- application 유저에게 loopers_qa 권한 부여
-- (MYSQL_USER 환경변수는 MYSQL_DATABASE에 대해서만 GRANT하므로 별도 필요)
GRANT ALL PRIVILEGES ON loopers_qa.* TO 'application'@'%';
FLUSH PRIVILEGES;
