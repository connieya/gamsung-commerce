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

-- 2. 유저 (20명, 실제와 유사한 아이디)
INSERT INTO users (user_id, email, birth_date, gender, created_at, updated_at, deleted_at) VALUES
('gunny',        'gunny@gmail.com',        '1995-05-15', 'MALE',   NOW(), NOW(), NULL),
('jieun_kim',    'jieun.kim@naver.com',     '1998-10-20', 'FEMALE', NOW(), NOW(), NULL),
('minjun.park',  'minjun.park@gmail.com',   '1992-03-08', 'MALE',   NOW(), NOW(), NULL),
('sooyoung99',   'sooyoung99@kakao.com',    '1999-07-12', 'FEMALE', NOW(), NOW(), NULL),
('hyunwoo_choi', 'hyunwoo.choi@naver.com',  '1994-11-30', 'MALE',   NOW(), NOW(), NULL),
('yuna_lee',     'yuna.lee@gmail.com',      '1997-02-14', 'FEMALE', NOW(), NOW(), NULL),
('dongwook',     'dongwook@kakao.com',      '1993-08-25', 'MALE',   NOW(), NOW(), NULL),
('sujin_ha',     'sujin.ha@naver.com',      '1996-04-18', 'FEMALE', NOW(), NOW(), NULL),
('junghoon.k',   'junghoon.k@gmail.com',    '1991-12-03', 'MALE',   NOW(), NOW(), NULL),
('minji_97',     'minji97@kakao.com',       '1997-06-22', 'FEMALE', NOW(), NOW(), NULL),
('taeyang.oh',   'taeyang.oh@naver.com',    '1995-09-10', 'MALE',   NOW(), NOW(), NULL),
('chaewon_s',    'chaewon.s@gmail.com',     '2000-01-28', 'FEMALE', NOW(), NOW(), NULL),
('seungho.j',    'seungho.j@kakao.com',     '1993-03-15', 'MALE',   NOW(), NOW(), NULL),
('yerin_moon',   'yerin.moon@naver.com',    '1998-08-07', 'FEMALE', NOW(), NOW(), NULL),
('woojin.ryu',   'woojin.ryu@gmail.com',    '1994-05-20', 'MALE',   NOW(), NOW(), NULL),
('haeun_0315',   'haeun0315@kakao.com',     '1999-03-15', 'FEMALE', NOW(), NOW(), NULL),
('doyeon_kang',  'doyeon.kang@naver.com',   '1996-11-11', 'FEMALE', NOW(), NOW(), NULL),
('jihwan_na',    'jihwan.na@gmail.com',     '1992-07-04', 'MALE',   NOW(), NOW(), NULL),
('nayoung.lim',  'nayoung.lim@kakao.com',  '1997-12-25', 'FEMALE', NOW(), NOW(), NULL),
('siwoo_han',    'siwoo.han@naver.com',     '1995-10-09', 'MALE',   NOW(), NOW(), NULL);

-- 3. 상품 (브랜드 id 1~7, 가격 원화, 이미지 URL)
INSERT INTO product (name, price, ref_brand_id, image_url, released_at, created_at, updated_at, deleted_at) VALUES
('오버핏 맨투맨 그레이',      39000, 1, 'https://picsum.photos/seed/musinsa-mtm/400/533',       '2024-09-01 00:00:00', NOW(), NOW(), NULL),
('오버핏 후드 네이비',        59000, 1, 'https://picsum.photos/seed/musinsa-hood/400/533',      '2024-10-01 00:00:00', NOW(), NOW(), NULL),
('와이드 슬랙스 베이지',      79000, 1, 'https://picsum.photos/seed/musinsa-slacks/400/533',    '2024-09-15 00:00:00', NOW(), NOW(), NULL),
('조거 팬츠 블랙',           49000, 1, 'https://picsum.photos/seed/musinsa-jogger/400/533',    '2024-10-10 00:00:00', NOW(), NOW(), NULL),
('크로스백 블랙',            45000, 1, 'https://picsum.photos/seed/musinsa-bag/400/533',       '2024-08-01 00:00:00', NOW(), NOW(), NULL),
('볼캡 로고',               29000, 1, 'https://picsum.photos/seed/musinsa-cap/400/533',       '2024-09-20 00:00:00', NOW(), NOW(), NULL),
('나이키 에어맥스 90',       159000, 2, 'https://picsum.photos/seed/nike-airmax90/400/533',     '2024-08-15 00:00:00', NOW(), NOW(), NULL),
('나이키 덩크 로우',         129000, 2, 'https://picsum.photos/seed/nike-dunklow/400/533',      '2024-07-20 00:00:00', NOW(), NOW(), NULL),
('아디다스 삼바 OG',         129000, 3, 'https://picsum.photos/seed/adidas-samba/400/533',      '2024-07-20 00:00:00', NOW(), NOW(), NULL),
('아디다스 가젤',            119000, 3, 'https://picsum.photos/seed/adidas-gazelle/400/533',    '2024-09-01 00:00:00', NOW(), NOW(), NULL),
('디스커버리 코치 재킷',     189000, 4, 'https://picsum.photos/seed/discovery-coach/400/533',   '2024-11-01 00:00:00', NOW(), NOW(), NULL),
('디스커버리 플리스 자켓',    139000, 4, 'https://picsum.photos/seed/discovery-fleece/400/533',  '2024-10-15 00:00:00', NOW(), NOW(), NULL),
('코듀로이 와이드 팬츠',      89000, 5, 'https://picsum.photos/seed/corduroy-wide/400/533',     '2024-10-01 00:00:00', NOW(), NOW(), NULL),
('울 싱글 코트 체크',        279000, 5, 'https://picsum.photos/seed/wool-coat-check/400/533',   '2024-11-10 00:00:00', NOW(), NOW(), NULL),
('스톤아일랜드 후드 스웨트',  329000, 6, 'https://picsum.photos/seed/stoneisland-hood/400/533',  '2024-08-20 00:00:00', NOW(), NOW(), NULL),
('무인양품 울 블렌드 코트',   199000, 7, 'https://picsum.photos/seed/muji-woolcoat/400/533',     '2024-11-01 00:00:00', NOW(), NOW(), NULL),
('무인양품 캐시미어 스웨터',   89000, 7, 'https://picsum.photos/seed/muji-cashmere/400/533',     '2024-10-01 00:00:00', NOW(), NOW(), NULL);

-- 4. 상품 좋아요 (user id 1~20, product id 1~17)
-- 인기 상품일수록 좋아요 多 — 나이키/아디다스 신발류 인기, 무신사스탠다드 중간, 기타 적음
INSERT INTO product_like (ref_user_id, ref_product_id, created_at, updated_at, deleted_at) VALUES
-- 상품 7 (나이키 에어맥스 90) — 15명
(1, 7, NOW(), NOW(), NULL), (2, 7, NOW(), NOW(), NULL), (3, 7, NOW(), NOW(), NULL),
(4, 7, NOW(), NOW(), NULL), (5, 7, NOW(), NOW(), NULL), (6, 7, NOW(), NOW(), NULL),
(7, 7, NOW(), NOW(), NULL), (8, 7, NOW(), NOW(), NULL), (9, 7, NOW(), NOW(), NULL),
(10, 7, NOW(), NOW(), NULL), (11, 7, NOW(), NOW(), NULL), (12, 7, NOW(), NOW(), NULL),
(13, 7, NOW(), NOW(), NULL), (14, 7, NOW(), NOW(), NULL), (15, 7, NOW(), NOW(), NULL),
-- 상품 9 (아디다스 삼바 OG) — 13명
(1, 9, NOW(), NOW(), NULL), (2, 9, NOW(), NOW(), NULL), (3, 9, NOW(), NOW(), NULL),
(4, 9, NOW(), NOW(), NULL), (6, 9, NOW(), NOW(), NULL), (7, 9, NOW(), NOW(), NULL),
(8, 9, NOW(), NOW(), NULL), (10, 9, NOW(), NOW(), NULL), (11, 9, NOW(), NOW(), NULL),
(12, 9, NOW(), NOW(), NULL), (14, 9, NOW(), NOW(), NULL), (16, 9, NOW(), NOW(), NULL),
(18, 9, NOW(), NOW(), NULL),
-- 상품 8 (나이키 덩크 로우) — 12명
(1, 8, NOW(), NOW(), NULL), (3, 8, NOW(), NOW(), NULL), (4, 8, NOW(), NOW(), NULL),
(5, 8, NOW(), NOW(), NULL), (7, 8, NOW(), NOW(), NULL), (9, 8, NOW(), NOW(), NULL),
(10, 8, NOW(), NOW(), NULL), (12, 8, NOW(), NOW(), NULL), (15, 8, NOW(), NOW(), NULL),
(16, 8, NOW(), NOW(), NULL), (17, 8, NOW(), NOW(), NULL), (19, 8, NOW(), NOW(), NULL),
-- 상품 15 (스톤아일랜드 후드 스웨트) — 10명
(1, 15, NOW(), NOW(), NULL), (3, 15, NOW(), NOW(), NULL), (5, 15, NOW(), NOW(), NULL),
(7, 15, NOW(), NOW(), NULL), (9, 15, NOW(), NOW(), NULL), (11, 15, NOW(), NOW(), NULL),
(13, 15, NOW(), NOW(), NULL), (15, 15, NOW(), NOW(), NULL), (18, 15, NOW(), NOW(), NULL),
(20, 15, NOW(), NOW(), NULL),
-- 상품 10 (아디다스 가젤) — 9명
(2, 10, NOW(), NOW(), NULL), (4, 10, NOW(), NOW(), NULL), (6, 10, NOW(), NOW(), NULL),
(8, 10, NOW(), NOW(), NULL), (10, 10, NOW(), NOW(), NULL), (13, 10, NOW(), NOW(), NULL),
(15, 10, NOW(), NOW(), NULL), (17, 10, NOW(), NOW(), NULL), (19, 10, NOW(), NOW(), NULL),
-- 상품 1 (오버핏 맨투맨 그레이) — 8명
(1, 1, NOW(), NOW(), NULL), (4, 1, NOW(), NOW(), NULL), (6, 1, NOW(), NOW(), NULL),
(8, 1, NOW(), NOW(), NULL), (10, 1, NOW(), NOW(), NULL), (14, 1, NOW(), NOW(), NULL),
(16, 1, NOW(), NOW(), NULL), (19, 1, NOW(), NOW(), NULL),
-- 상품 2 (오버핏 후드 네이비) — 7명
(2, 2, NOW(), NOW(), NULL), (5, 2, NOW(), NOW(), NULL), (7, 2, NOW(), NOW(), NULL),
(9, 2, NOW(), NOW(), NULL), (12, 2, NOW(), NOW(), NULL), (17, 2, NOW(), NOW(), NULL),
(20, 2, NOW(), NOW(), NULL),
-- 상품 11 (디스커버리 코치 재킷) — 6명
(1, 11, NOW(), NOW(), NULL), (3, 11, NOW(), NOW(), NULL), (6, 11, NOW(), NOW(), NULL),
(11, 11, NOW(), NOW(), NULL), (14, 11, NOW(), NOW(), NULL), (18, 11, NOW(), NOW(), NULL),
-- 상품 14 (울 싱글 코트 체크) — 5명
(2, 14, NOW(), NOW(), NULL), (5, 14, NOW(), NOW(), NULL), (8, 14, NOW(), NOW(), NULL),
(13, 14, NOW(), NOW(), NULL), (20, 14, NOW(), NOW(), NULL),
-- 상품 3 (와이드 슬랙스 베이지) — 5명
(3, 3, NOW(), NOW(), NULL), (7, 3, NOW(), NOW(), NULL), (10, 3, NOW(), NOW(), NULL),
(15, 3, NOW(), NOW(), NULL), (19, 3, NOW(), NOW(), NULL),
-- 상품 12 (디스커버리 플리스 자켓) — 4명
(4, 12, NOW(), NOW(), NULL), (9, 12, NOW(), NOW(), NULL), (14, 12, NOW(), NOW(), NULL),
(17, 12, NOW(), NOW(), NULL),
-- 상품 16 (무인양품 울 블렌드 코트) — 4명
(6, 16, NOW(), NOW(), NULL), (10, 16, NOW(), NOW(), NULL), (13, 16, NOW(), NOW(), NULL),
(18, 16, NOW(), NOW(), NULL),
-- 상품 4 (조거 팬츠 블랙) — 3명
(2, 4, NOW(), NOW(), NULL), (11, 4, NOW(), NOW(), NULL), (16, 4, NOW(), NOW(), NULL),
-- 상품 13 (코듀로이 와이드 팬츠) — 3명
(5, 13, NOW(), NOW(), NULL), (12, 13, NOW(), NOW(), NULL), (20, 13, NOW(), NOW(), NULL),
-- 상품 17 (무인양품 캐시미어 스웨터) — 3명
(3, 17, NOW(), NOW(), NULL), (8, 17, NOW(), NOW(), NULL), (15, 17, NOW(), NOW(), NULL),
-- 상품 5 (크로스백 블랙) — 2명
(7, 5, NOW(), NOW(), NULL), (14, 5, NOW(), NOW(), NULL),
-- 상품 6 (볼캡 로고) — 1명
(11, 6, NOW(), NOW(), NULL);

-- 5. like_summary (비정규화 테이블, product_like 건수와 정합성 유지)
INSERT INTO like_summary (like_count, target_id, target_type, created_at, updated_at, deleted_at) VALUES
(15, 7,  'PRODUCT', NOW(), NOW(), NULL),
(13, 9,  'PRODUCT', NOW(), NOW(), NULL),
(12, 8,  'PRODUCT', NOW(), NOW(), NULL),
(10, 15, 'PRODUCT', NOW(), NOW(), NULL),
( 9, 10, 'PRODUCT', NOW(), NOW(), NULL),
( 8, 1,  'PRODUCT', NOW(), NOW(), NULL),
( 7, 2,  'PRODUCT', NOW(), NOW(), NULL),
( 6, 11, 'PRODUCT', NOW(), NOW(), NULL),
( 5, 14, 'PRODUCT', NOW(), NOW(), NULL),
( 5, 3,  'PRODUCT', NOW(), NOW(), NULL),
( 4, 12, 'PRODUCT', NOW(), NOW(), NULL),
( 4, 16, 'PRODUCT', NOW(), NOW(), NULL),
( 3, 4,  'PRODUCT', NOW(), NOW(), NULL),
( 3, 13, 'PRODUCT', NOW(), NOW(), NULL),
( 3, 17, 'PRODUCT', NOW(), NOW(), NULL),
( 2, 5,  'PRODUCT', NOW(), NOW(), NULL),
( 1, 6,  'PRODUCT', NOW(), NOW(), NULL);
