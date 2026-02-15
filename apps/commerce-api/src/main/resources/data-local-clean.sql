-- local 프로필 기동 시 DataLocalLoader에서 실행: 시드 대상 테이블만 비운다 (ddl-auto 변경 없이 사용)
-- FK 순서: product_like → like_summary → product → users → brand

-- 스키마 마이그레이션: image_url 컬럼 추가 (이미 존재하면 무시)
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product' AND COLUMN_NAME = 'image_url');
SET @stmt = IF(@col_exists = 0, 'ALTER TABLE product ADD COLUMN image_url VARCHAR(500) DEFAULT NULL', 'SELECT 1');
PREPARE migration FROM @stmt;
EXECUTE migration;
DEALLOCATE PREPARE migration;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE product_like;
TRUNCATE TABLE like_summary;
TRUNCATE TABLE product;
TRUNCATE TABLE users;
TRUNCATE TABLE brand;

SET FOREIGN_KEY_CHECKS = 1;
