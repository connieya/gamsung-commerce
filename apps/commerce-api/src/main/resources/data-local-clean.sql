-- local 프로필 기동 시 DataLocalLoader에서 실행: 시드 대상 테이블만 비운다 (ddl-auto 변경 없이 사용)
-- FK 순서: product_like → like_summary → product → users → brand

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE product_like;
TRUNCATE TABLE like_summary;
TRUNCATE TABLE product;
TRUNCATE TABLE users;
TRUNCATE TABLE brand;

SET FOREIGN_KEY_CHECKS = 1;
