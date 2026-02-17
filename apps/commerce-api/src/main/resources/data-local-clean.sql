-- local 프로필 기동 시 DataLocalLoader에서 실행: 시드 대상 테이블만 비운다 (ddl-auto 변경 없이 사용)
-- FK 순서: product_like → like_summary → product → users → brand

-- 스키마 마이그레이션: image_url 컬럼 추가 (이미 존재하면 무시)
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product' AND COLUMN_NAME = 'image_url');
SET @stmt = IF(@col_exists = 0, 'ALTER TABLE product ADD COLUMN image_url VARCHAR(500) DEFAULT NULL', 'SELECT 1');
PREPARE migration FROM @stmt;
EXECUTE migration;
DEALLOCATE PREPARE migration;

-- 스키마 마이그레이션: 모든 테이블 생성 (이미 존재하면 무시)
-- 1. brand
SET @table_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'brand');
SET @stmt = IF(@table_exists = 0,
    'CREATE TABLE `brand` (
      `id` BIGINT NOT NULL AUTO_INCREMENT,
      `created_at` DATETIME(6) NOT NULL,
      `updated_at` DATETIME(6) NOT NULL,
      `deleted_at` DATETIME(6) DEFAULT NULL,
      `name` VARCHAR(255) DEFAULT NULL,
      `description` VARCHAR(255) DEFAULT NULL,
      PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci',
    'SELECT 1');
PREPARE migration FROM @stmt;
EXECUTE migration;
DEALLOCATE PREPARE migration;

-- 2. users
SET @table_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users');
SET @stmt = IF(@table_exists = 0,
    'CREATE TABLE `users` (
      `id` BIGINT NOT NULL AUTO_INCREMENT,
      `created_at` DATETIME(6) NOT NULL,
      `updated_at` DATETIME(6) NOT NULL,
      `deleted_at` DATETIME(6) DEFAULT NULL,
      `user_id` VARCHAR(255) NOT NULL,
      `email` VARCHAR(255) DEFAULT NULL,
      `birth_date` DATE NOT NULL,
      `gender` VARCHAR(255) DEFAULT NULL,
      PRIMARY KEY (`id`),
      UNIQUE KEY `UK_user_id` (`user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci',
    'SELECT 1');
PREPARE migration FROM @stmt;
EXECUTE migration;
DEALLOCATE PREPARE migration;

-- 3. product
SET @table_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product');
SET @stmt = IF(@table_exists = 0,
    'CREATE TABLE `product` (
      `id` BIGINT NOT NULL AUTO_INCREMENT,
      `created_at` DATETIME(6) NOT NULL,
      `updated_at` DATETIME(6) NOT NULL,
      `deleted_at` DATETIME(6) DEFAULT NULL,
      `name` VARCHAR(255) DEFAULT NULL,
      `price` BIGINT DEFAULT NULL,
      `ref_brand_id` BIGINT DEFAULT NULL,
      `image_url` VARCHAR(500) DEFAULT NULL,
      `released_at` DATETIME(6) DEFAULT NULL,
      PRIMARY KEY (`id`),
      KEY `idx_ref_brand_id` (`ref_brand_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci',
    'SELECT 1');
PREPARE migration FROM @stmt;
EXECUTE migration;
DEALLOCATE PREPARE migration;

-- 4. product_like
SET @table_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'product_like');
SET @stmt = IF(@table_exists = 0,
    'CREATE TABLE `product_like` (
      `id` BIGINT NOT NULL AUTO_INCREMENT,
      `created_at` DATETIME(6) NOT NULL,
      `updated_at` DATETIME(6) NOT NULL,
      `deleted_at` DATETIME(6) DEFAULT NULL,
      `ref_user_id` BIGINT NOT NULL,
      `ref_product_id` BIGINT NOT NULL,
      PRIMARY KEY (`id`),
      UNIQUE KEY `UK_user_product` (`ref_user_id`, `ref_product_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci',
    'SELECT 1');
PREPARE migration FROM @stmt;
EXECUTE migration;
DEALLOCATE PREPARE migration;

-- 5. like_summary
SET @table_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'like_summary');
SET @stmt = IF(@table_exists = 0,
    'CREATE TABLE `like_summary` (
      `id` BIGINT NOT NULL AUTO_INCREMENT,
      `created_at` DATETIME(6) NOT NULL,
      `updated_at` DATETIME(6) NOT NULL,
      `deleted_at` DATETIME(6) DEFAULT NULL,
      `like_count` BIGINT DEFAULT NULL,
      `target_id` BIGINT DEFAULT NULL,
      `target_type` VARCHAR(255) DEFAULT NULL,
      PRIMARY KEY (`id`),
      UNIQUE KEY `UK_target` (`target_id`, `target_type`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci',
    'SELECT 1');
PREPARE migration FROM @stmt;
EXECUTE migration;
DEALLOCATE PREPARE migration;

-- 6. view_product
SET @table_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'view_product');
SET @stmt = IF(@table_exists = 0,
    'CREATE TABLE `view_product` (
      `id` BIGINT NOT NULL AUTO_INCREMENT,
      `created_at` DATETIME(6) NOT NULL,
      `updated_at` DATETIME(6) NOT NULL,
      `deleted_at` DATETIME(6) DEFAULT NULL,
      `ref_product_id` BIGINT NOT NULL,
      `view_count` BIGINT NOT NULL,
      PRIMARY KEY (`id`),
      KEY `idx_ref_product_id` (`ref_product_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci',
    'SELECT 1');
PREPARE migration FROM @stmt;
EXECUTE migration;
DEALLOCATE PREPARE migration;

-- 7. stock
SET @table_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'stock');
SET @stmt = IF(@table_exists = 0,
    'CREATE TABLE `stock` (
      `id` BIGINT NOT NULL AUTO_INCREMENT,
      `created_at` DATETIME(6) NOT NULL,
      `updated_at` DATETIME(6) NOT NULL,
      `deleted_at` DATETIME(6) DEFAULT NULL,
      `ref_product_id` BIGINT NOT NULL,
      `quantity` BIGINT DEFAULT NULL,
      PRIMARY KEY (`id`),
      KEY `idx_ref_product_id` (`ref_product_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci',
    'SELECT 1');
PREPARE migration FROM @stmt;
EXECUTE migration;
DEALLOCATE PREPARE migration;

-- 8. cart
SET @table_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cart');
SET @stmt = IF(@table_exists = 0,
    'CREATE TABLE `cart` (
      `id` BIGINT NOT NULL AUTO_INCREMENT,
      `created_at` DATETIME(6) NOT NULL,
      `updated_at` DATETIME(6) NOT NULL,
      `deleted_at` DATETIME(6) DEFAULT NULL,
      `user_id` BIGINT NOT NULL,
      PRIMARY KEY (`id`),
      UNIQUE KEY `UK_user_id` (`user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci',
    'SELECT 1');
PREPARE migration FROM @stmt;
EXECUTE migration;
DEALLOCATE PREPARE migration;

-- 9. cart_item
SET @table_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cart_item');
SET @stmt = IF(@table_exists = 0,
    'CREATE TABLE `cart_item` (
      `id` BIGINT NOT NULL AUTO_INCREMENT,
      `created_at` DATETIME(6) NOT NULL,
      `updated_at` DATETIME(6) NOT NULL,
      `deleted_at` DATETIME(6) DEFAULT NULL,
      `product_id` BIGINT NOT NULL,
      `quantity` BIGINT NOT NULL,
      `price` BIGINT NOT NULL,
      `cart_id` BIGINT DEFAULT NULL,
      PRIMARY KEY (`id`),
      KEY `idx_cart_id` (`cart_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci',
    'SELECT 1');
PREPARE migration FROM @stmt;
EXECUTE migration;
DEALLOCATE PREPARE migration;

-- 10. orders
SET @table_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'orders');
SET @stmt = IF(@table_exists = 0,
    'CREATE TABLE `orders` (
      `id` BIGINT NOT NULL AUTO_INCREMENT,
      `created_at` DATETIME(6) NOT NULL,
      `updated_at` DATETIME(6) NOT NULL,
      `deleted_at` DATETIME(6) DEFAULT NULL,
      `total_amount` BIGINT NOT NULL,
      `order_number` VARCHAR(255) NOT NULL,
      `ref_user_id` BIGINT NOT NULL,
      `discount_amount` BIGINT DEFAULT NULL,
      `order_status` VARCHAR(255) NOT NULL,
      PRIMARY KEY (`id`),
      KEY `idx_ref_user_id` (`ref_user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci',
    'SELECT 1');
PREPARE migration FROM @stmt;
EXECUTE migration;
DEALLOCATE PREPARE migration;

-- 11. order_line
SET @table_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_line');
SET @stmt = IF(@table_exists = 0,
    'CREATE TABLE `order_line` (
      `id` BIGINT NOT NULL AUTO_INCREMENT,
      `created_at` DATETIME(6) NOT NULL,
      `updated_at` DATETIME(6) NOT NULL,
      `deleted_at` DATETIME(6) DEFAULT NULL,
      `product_id` BIGINT DEFAULT NULL,
      `quantity` BIGINT DEFAULT NULL,
      `order_price` BIGINT DEFAULT NULL,
      `order_id` BIGINT DEFAULT NULL,
      PRIMARY KEY (`id`),
      KEY `idx_order_id` (`order_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci',
    'SELECT 1');
PREPARE migration FROM @stmt;
EXECUTE migration;
DEALLOCATE PREPARE migration;

-- 12. payment
SET @table_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment');
SET @stmt = IF(@table_exists = 0,
    'CREATE TABLE `payment` (
      `id` BIGINT NOT NULL AUTO_INCREMENT,
      `created_at` DATETIME(6) NOT NULL,
      `updated_at` DATETIME(6) NOT NULL,
      `deleted_at` DATETIME(6) DEFAULT NULL,
      `amount` BIGINT NOT NULL,
      `ref_order_id` BIGINT NOT NULL,
      `order_number` VARCHAR(255) NOT NULL,
      `ref_user_id` BIGINT NOT NULL,
      `method` VARCHAR(255) NOT NULL,
      `status` VARCHAR(255) NOT NULL,
      PRIMARY KEY (`id`),
      UNIQUE KEY `UK_order_number` (`order_number`),
      KEY `idx_ref_order_id` (`ref_order_id`),
      KEY `idx_ref_user_id` (`ref_user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci',
    'SELECT 1');
PREPARE migration FROM @stmt;
EXECUTE migration;
DEALLOCATE PREPARE migration;

-- 13. payment_attempt
SET @table_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment_attempt');
SET @stmt = IF(@table_exists = 0,
    'CREATE TABLE `payment_attempt` (
      `id` BIGINT NOT NULL AUTO_INCREMENT,
      `created_at` DATETIME(6) NOT NULL,
      `updated_at` DATETIME(6) NOT NULL,
      `deleted_at` DATETIME(6) DEFAULT NULL,
      `transaction_key` VARCHAR(255) DEFAULT NULL,
      `ref_payment_id` BIGINT NOT NULL,
      `ref_order_number` VARCHAR(255) NOT NULL,
      `attempt_status` VARCHAR(255) DEFAULT NULL,
      PRIMARY KEY (`id`),
      KEY `idx_ref_payment_id` (`ref_payment_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci',
    'SELECT 1');
PREPARE migration FROM @stmt;
EXECUTE migration;
DEALLOCATE PREPARE migration;

-- 14. coupon
SET @table_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'coupon');
SET @stmt = IF(@table_exists = 0,
    'CREATE TABLE `coupon` (
      `id` BIGINT NOT NULL AUTO_INCREMENT,
      `created_at` DATETIME(6) NOT NULL,
      `updated_at` DATETIME(6) NOT NULL,
      `deleted_at` DATETIME(6) DEFAULT NULL,
      `coupon_name` VARCHAR(255) NOT NULL,
      `coupon_type` VARCHAR(255) DEFAULT NULL,
      `value` BIGINT DEFAULT NULL,
      PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci',
    'SELECT 1');
PREPARE migration FROM @stmt;
EXECUTE migration;
DEALLOCATE PREPARE migration;

-- 15. user_coupon
SET @table_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_coupon');
SET @stmt = IF(@table_exists = 0,
    'CREATE TABLE `user_coupon` (
      `id` BIGINT NOT NULL AUTO_INCREMENT,
      `created_at` DATETIME(6) NOT NULL,
      `updated_at` DATETIME(6) NOT NULL,
      `deleted_at` DATETIME(6) DEFAULT NULL,
      `coupon_id` BIGINT DEFAULT NULL,
      `user_id` BIGINT DEFAULT NULL,
      `used` BIT(1) NOT NULL,
      `version` BIGINT DEFAULT NULL,
      PRIMARY KEY (`id`),
      UNIQUE KEY `UK_coupon_user` (`coupon_id`, `user_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci',
    'SELECT 1');
PREPARE migration FROM @stmt;
EXECUTE migration;
DEALLOCATE PREPARE migration;

-- 16. points
SET @table_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'points');
SET @stmt = IF(@table_exists = 0,
    'CREATE TABLE `points` (
      `id` BIGINT NOT NULL AUTO_INCREMENT,
      `created_at` DATETIME(6) NOT NULL,
      `updated_at` DATETIME(6) NOT NULL,
      `deleted_at` DATETIME(6) DEFAULT NULL,
      `user_id` VARCHAR(255) DEFAULT NULL,
      `value` BIGINT DEFAULT NULL,
      PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci',
    'SELECT 1');
PREPARE migration FROM @stmt;
EXECUTE migration;
DEALLOCATE PREPARE migration;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE product_like;
TRUNCATE TABLE like_summary;
TRUNCATE TABLE product;
TRUNCATE TABLE users;
TRUNCATE TABLE brand;
TRUNCATE TABLE view_product;
TRUNCATE TABLE stock;
TRUNCATE TABLE cart_item;
TRUNCATE TABLE cart;
TRUNCATE TABLE order_line;
TRUNCATE TABLE orders;
TRUNCATE TABLE payment_attempt;
TRUNCATE TABLE payment;
TRUNCATE TABLE user_coupon;
TRUNCATE TABLE coupon;
TRUNCATE TABLE points;

SET FOREIGN_KEY_CHECKS = 1;
