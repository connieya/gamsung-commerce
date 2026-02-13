-- local 프로필 기동 시 자동 실행: 무신사 스타일 시드 데이터 (클라이언트 웹 확인용)
-- 실행 순서: brand → users → product → product_like → like_summary (FK·정합성 순서)

-- 1. 브랜드
INSERT INTO brand (name, description, created_at, updated_at, deleted_at) VALUES
('무신사스탠다드', '무신사 자체 브랜드', NOW(), NOW(), NULL),
('나이키', 'Just Do It', NOW(), NOW(), NULL),
('아디다스', 'Impossible is Nothing', NOW(), NOW(), NULL),
('디스커버리익스페디션', '아웃도어·라이프스타일', NOW(), NOW(), NULL),
('코듀로이', '캐주얼 웨어', NOW(), NOW(), NULL),
('스톤아일랜드', '이탈리안 스트리트웨어', NOW(), NOW(), NULL),
('무인양품', 'No Brand, Good Product', NOW(), NOW(), NULL);

-- 2. 유저
INSERT INTO users (user_id, email, birth_date, gender, created_at, updated_at, deleted_at) VALUES
('user1', 'user1@example.com', '1995-05-15', 'MALE', NOW(), NOW(), NULL),
('user2', 'user2@example.com', '1998-10-20', 'FEMALE', NOW(), NOW(), NULL),
('user3', 'user3@example.com', '1992-03-08', 'MALE', NOW(), NOW(), NULL);

-- 3. 상품 (브랜드 id 1~7, 가격 원화)
INSERT INTO product (name, price, ref_brand_id, released_at, created_at, updated_at, deleted_at) VALUES
('오버핏 맨투맨 그레이', 39000, 1, '2024-09-01 00:00:00', NOW(), NOW(), NULL),
('오버핏 후드 네이비', 59000, 1, '2024-10-01 00:00:00', NOW(), NOW(), NULL),
('와이드 슬랙스 베이지', 79000, 1, '2024-09-15 00:00:00', NOW(), NOW(), NULL),
('조거 팬츠 블랙', 49000, 1, '2024-10-10 00:00:00', NOW(), NOW(), NULL),
('크로스백 블랙', 45000, 1, '2024-08-01 00:00:00', NOW(), NOW(), NULL),
('볼캡 로고', 29000, 1, '2024-09-20 00:00:00', NOW(), NOW(), NULL),
('나이키 에어맥스 90', 159000, 2, '2024-08-15 00:00:00', NOW(), NOW(), NULL),
('나이키 덩크 로우', 129000, 2, '2024-07-20 00:00:00', NOW(), NOW(), NULL),
('아디다스 삼바 OG', 129000, 3, '2024-07-20 00:00:00', NOW(), NOW(), NULL),
('아디다스 가젤', 119000, 3, '2024-09-01 00:00:00', NOW(), NOW(), NULL),
('디스커버리 코치 재킷', 189000, 4, '2024-11-01 00:00:00', NOW(), NOW(), NULL),
('디스커버리 플리스 자켓', 139000, 4, '2024-10-15 00:00:00', NOW(), NOW(), NULL),
('코듀로이 와이드 팬츠', 89000, 5, '2024-10-01 00:00:00', NOW(), NOW(), NULL),
('울 싱글 코트 체크', 279000, 5, '2024-11-10 00:00:00', NOW(), NOW(), NULL),
('스톤아일랜드 후드 스웨트', 329000, 6, '2024-08-20 00:00:00', NOW(), NOW(), NULL),
('무인양품 울 블렌드 코트', 199000, 7, '2024-11-01 00:00:00', NOW(), NOW(), NULL),
('무인양품 캐시미어 스웨터', 89000, 7, '2024-10-01 00:00:00', NOW(), NOW(), NULL);

-- 4. 상품 좋아요 (선택, user id 1~3, product id 1~17)
INSERT INTO product_like (ref_user_id, ref_product_id, created_at, updated_at, deleted_at) VALUES
(1, 1, NOW(), NOW(), NULL),
(1, 7, NOW(), NOW(), NULL),
(1, 11, NOW(), NOW(), NULL),
(2, 2, NOW(), NOW(), NULL),
(2, 8, NOW(), NOW(), NULL),
(2, 15, NOW(), NOW(), NULL),
(3, 3, NOW(), NOW(), NULL),
(3, 9, NOW(), NOW(), NULL),
(3, 16, NOW(), NOW(), NULL);

-- 5. like_summary (비정규화 테이블, product_like 건수와 정합성 유지)
-- 상품별 좋아요 수: product 1,2,3,7,8,9,11,15,16 각 1건
INSERT INTO like_summary (like_count, target_id, target_type, created_at, updated_at, deleted_at) VALUES
(1, 1, 'PRODUCT', NOW(), NOW(), NULL),
(1, 2, 'PRODUCT', NOW(), NOW(), NULL),
(1, 3, 'PRODUCT', NOW(), NOW(), NULL),
(1, 7, 'PRODUCT', NOW(), NOW(), NULL),
(1, 8, 'PRODUCT', NOW(), NOW(), NULL),
(1, 9, 'PRODUCT', NOW(), NOW(), NULL),
(1, 11, 'PRODUCT', NOW(), NOW(), NULL),
(1, 15, 'PRODUCT', NOW(), NOW(), NULL),
(1, 16, 'PRODUCT', NOW(), NOW(), NULL);
