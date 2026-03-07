-- local 프로필 기동 시 자동 실행: 무신사 스타일 시드 데이터 (클라이언트 웹 확인용)
-- 실행 순서: category → brand → users → product → product_like → like_summary (FK·정합성 순서)

-- 1. 카테고리 (depth 1: 대분류, depth 2: 소분류)
INSERT INTO category (name, parent_id, depth, display_order, created_at, updated_at, deleted_at) VALUES
-- 대분류 (id 1~6)
('상의',   NULL, 1, 1, NOW(), NOW(), NULL),
('아우터', NULL, 1, 2, NOW(), NOW(), NULL),
('바지',   NULL, 1, 3, NOW(), NOW(), NULL),
('신발',   NULL, 1, 4, NOW(), NOW(), NULL),
('가방',   NULL, 1, 5, NOW(), NOW(), NULL),
('소품',   NULL, 1, 6, NOW(), NOW(), NULL);

INSERT INTO category (name, parent_id, depth, display_order, created_at, updated_at, deleted_at) VALUES
-- 상의 하위 (parent_id=1, id 7~11)
('긴소매 티셔츠',       1, 2, 1, NOW(), NOW(), NULL),
('맨투맨/스웨트',        1, 2, 2, NOW(), NOW(), NULL),
('셔츠/블라우스',        1, 2, 3, NOW(), NOW(), NULL),
('후드 티셔츠',          1, 2, 4, NOW(), NOW(), NULL),
('반소매 티셔츠',        1, 2, 5, NOW(), NOW(), NULL),
-- 아우터 하위 (parent_id=2, id 12~17)
('후드 집업',            2, 2, 1, NOW(), NOW(), NULL),
('블루종/MA-1',          2, 2, 2, NOW(), NOW(), NULL),
('레더/라이더스 재킷',    2, 2, 3, NOW(), NOW(), NULL),
('슈트/블레이저 재킷',    2, 2, 4, NOW(), NOW(), NULL),
('카디건',               2, 2, 5, NOW(), NOW(), NULL),
('경량패딩/패딩 베스트',   2, 2, 6, NOW(), NOW(), NULL),
-- 바지 하위 (parent_id=3, id 18~23)
('데님 팬츠',            3, 2, 1, NOW(), NOW(), NULL),
('트레이닝/조거 팬츠',    3, 2, 2, NOW(), NOW(), NULL),
('코튼 팬츠',            3, 2, 3, NOW(), NOW(), NULL),
('슈트 팬츠/슬랙스',      3, 2, 4, NOW(), NOW(), NULL),
('숏 팬츠',              3, 2, 5, NOW(), NOW(), NULL),
('레깅스',               3, 2, 6, NOW(), NOW(), NULL),
-- 신발 하위 (parent_id=4, id 24~28)
('스니커즈',             4, 2, 1, NOW(), NOW(), NULL),
('스포츠화',             4, 2, 2, NOW(), NOW(), NULL),
('구두',                 4, 2, 3, NOW(), NOW(), NULL),
('부츠/워커',            4, 2, 4, NOW(), NOW(), NULL),
('샌들/슬리퍼',          4, 2, 5, NOW(), NOW(), NULL),
-- 가방 하위 (parent_id=5, id 29~38)
('메신저/크로스 백',      5, 2, 1, NOW(), NOW(), NULL),
('숄더 백',              5, 2, 2, NOW(), NOW(), NULL),
('백팩',                 5, 2, 3, NOW(), NOW(), NULL),
('토트백',               5, 2, 4, NOW(), NOW(), NULL),
('에코백',               5, 2, 5, NOW(), NOW(), NULL),
('보스턴/더플백',         5, 2, 6, NOW(), NOW(), NULL),
('웨이스트 백',           5, 2, 7, NOW(), NOW(), NULL),
('파우치 백',             5, 2, 8, NOW(), NOW(), NULL),
('브리프 케이스',         5, 2, 9, NOW(), NOW(), NULL),
('캐리어',               5, 2, 10, NOW(), NOW(), NULL),
-- 소품 하위 (parent_id=6, id 39~44)
('모자/머플러',           6, 2, 1, NOW(), NOW(), NULL),
('주얼리',               6, 2, 2, NOW(), NOW(), NULL),
('양말/레그웨어',         6, 2, 3, NOW(), NOW(), NULL),
('선글라스/안경테',       6, 2, 4, NOW(), NOW(), NULL),
('시계',                 6, 2, 5, NOW(), NOW(), NULL),
('벨트',                 6, 2, 6, NOW(), NOW(), NULL);

-- 2. 브랜드 (id 1~7)
INSERT INTO brand (name, description, created_at, updated_at, deleted_at) VALUES
('무신사스탠다드', '무신사 자체 브랜드', NOW(), NOW(), NULL),
('나이키', 'Just Do It', NOW(), NOW(), NULL),
('아디다스', 'Impossible is Nothing', NOW(), NOW(), NULL),
('디스커버리익스페디션', '아웃도어·라이프스타일', NOW(), NOW(), NULL),
('코듀로이', '캐주얼 웨어', NOW(), NOW(), NULL),
('스톤아일랜드', '이탈리안 스트리트웨어', NOW(), NOW(), NULL),
('무인양품', 'No Brand, Good Product', NOW(), NOW(), NULL);

-- 3. 유저 (20명, 실제와 유사한 아이디)
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

-- 4. 상품 (브랜드 id 1~7, 카테고리 id = 소분류, 가격 원화, 이미지 URL)
-- 소분류 ID 참조: 긴소매7 맨투맨8 셔츠9 후드10 반소매11 | 후드집업12 블루종13 레더14 슈트블레이저15 카디건16 경량패딩17
--                데님18 조거19 코튼20 슬랙스21 숏22 레깅스23 | 스니커즈24 스포츠25 구두26 부츠27 샌들28
--                메신저크로스29 숄더30 백팩31 토트32 에코33 보스턴34 웨이스트35 파우치36 브리프37 캐리어38
--                모자머플러39 주얼리40 양말41 선글라스42 시계43 벨트44
INSERT INTO product (name, price, ref_brand_id, ref_category_id, image_url, released_at, created_at, updated_at, deleted_at) VALUES
-- 기존 상품 (id 1~17)
('오버핏 맨투맨 그레이',      39000, 1, 8,  'https://picsum.photos/seed/musinsa-mtm/400/533',       '2024-09-01 00:00:00', NOW(), NOW(), NULL),
('오버핏 후드 네이비',        59000, 1, 10, 'https://picsum.photos/seed/musinsa-hood/400/533',      '2024-10-01 00:00:00', NOW(), NOW(), NULL),
('와이드 슬랙스 베이지',      79000, 1, 21, 'https://picsum.photos/seed/musinsa-slacks/400/533',    '2024-09-15 00:00:00', NOW(), NOW(), NULL),
('조거 팬츠 블랙',           49000, 1, 19, 'https://picsum.photos/seed/musinsa-jogger/400/533',    '2024-10-10 00:00:00', NOW(), NOW(), NULL),
('크로스백 블랙',            45000, 1, 29, 'https://picsum.photos/seed/musinsa-bag/400/533',       '2024-08-01 00:00:00', NOW(), NOW(), NULL),
('볼캡 로고',               29000, 1, 39, 'https://picsum.photos/seed/musinsa-cap/400/533',       '2024-09-20 00:00:00', NOW(), NOW(), NULL),
('나이키 에어맥스 90',       159000, 2, 24, 'https://picsum.photos/seed/nike-airmax90/400/533',     '2024-08-15 00:00:00', NOW(), NOW(), NULL),
('나이키 덩크 로우',         129000, 2, 24, 'https://picsum.photos/seed/nike-dunklow/400/533',      '2024-07-20 00:00:00', NOW(), NOW(), NULL),
('아디다스 삼바 OG',         129000, 3, 24, 'https://picsum.photos/seed/adidas-samba/400/533',      '2024-07-20 00:00:00', NOW(), NOW(), NULL),
('아디다스 가젤',            119000, 3, 24, 'https://picsum.photos/seed/adidas-gazelle/400/533',    '2024-09-01 00:00:00', NOW(), NOW(), NULL),
('디스커버리 코치 재킷',     189000, 4, 13, 'https://picsum.photos/seed/discovery-coach/400/533',   '2024-11-01 00:00:00', NOW(), NOW(), NULL),
('디스커버리 플리스 자켓',    139000, 4, 16, 'https://picsum.photos/seed/discovery-fleece/400/533',  '2024-10-15 00:00:00', NOW(), NOW(), NULL),
('코듀로이 와이드 팬츠',      89000, 5, 20, 'https://picsum.photos/seed/corduroy-wide/400/533',     '2024-10-01 00:00:00', NOW(), NOW(), NULL),
('울 싱글 코트 체크',        279000, 5, 15, 'https://picsum.photos/seed/wool-coat-check/400/533',   '2024-11-10 00:00:00', NOW(), NOW(), NULL),
('스톤아일랜드 후드 스웨트',  329000, 6, 10, 'https://picsum.photos/seed/stoneisland-hood/400/533',  '2024-08-20 00:00:00', NOW(), NOW(), NULL),
('무인양품 울 블렌드 코트',   199000, 7, 16, 'https://picsum.photos/seed/muji-woolcoat/400/533',     '2024-11-01 00:00:00', NOW(), NOW(), NULL),
('무인양품 캐시미어 스웨터',   89000, 7, 7,  'https://picsum.photos/seed/muji-cashmere/400/533',     '2024-10-01 00:00:00', NOW(), NOW(), NULL),
-- 추가 상품: 빈 카테고리 채우기 (id 18~43)
-- 상의
('오버핏 옥스포드 셔츠 화이트', 49000, 1, 9,  'https://picsum.photos/seed/musinsa-shirt/400/533',     '2024-09-10 00:00:00', NOW(), NOW(), NULL),
('에센셜 반소매 티 블랙',      19000, 1, 11, 'https://picsum.photos/seed/musinsa-tee/400/533',       '2024-06-01 00:00:00', NOW(), NOW(), NULL),
-- 아우터
('나이키 테크 플리스 풀짚',   179000, 2, 12, 'https://picsum.photos/seed/nike-techfleece/400/533',   '2024-10-01 00:00:00', NOW(), NOW(), NULL),
('싱글 라이더스 재킷 블랙',   199000, 1, 14, 'https://picsum.photos/seed/musinsa-rider/400/533',     '2024-09-15 00:00:00', NOW(), NOW(), NULL),
('디스커버리 경량 패딩 베스트', 129000, 4, 17, 'https://picsum.photos/seed/discovery-vest/400/533',    '2024-11-15 00:00:00', NOW(), NOW(), NULL),
-- 바지
('레귤러 데님 팬츠 인디고',    59000, 1, 18, 'https://picsum.photos/seed/musinsa-denim/400/533',     '2024-08-20 00:00:00', NOW(), NOW(), NULL),
('나이키 스포츠웨어 숏 팬츠',   45000, 2, 22, 'https://picsum.photos/seed/nike-shorts/400/533',       '2024-05-15 00:00:00', NOW(), NOW(), NULL),
('아디다스 3S 레깅스 블랙',     39000, 3, 23, 'https://picsum.photos/seed/adidas-leggings/400/533',   '2024-06-01 00:00:00', NOW(), NOW(), NULL),
-- 신발
('나이키 에어 줌 페가수스 41',  159000, 2, 25, 'https://picsum.photos/seed/nike-pegasus/400/533',      '2024-07-01 00:00:00', NOW(), NOW(), NULL),
('클래식 더비 슈즈 브라운',     89000, 1, 26, 'https://picsum.photos/seed/musinsa-derby/400/533',     '2024-09-01 00:00:00', NOW(), NOW(), NULL),
('사이드짚 첼시 부츠 블랙',     99000, 1, 27, 'https://picsum.photos/seed/musinsa-chelsea/400/533',   '2024-10-20 00:00:00', NOW(), NOW(), NULL),
('나이키 빅토리 원 슬라이드',    39000, 2, 28, 'https://picsum.photos/seed/nike-slide/400/533',        '2024-05-01 00:00:00', NOW(), NOW(), NULL),
-- 가방
('캔버스 숄더백 네이비',        35000, 1, 30, 'https://picsum.photos/seed/musinsa-shoulder/400/533',  '2024-08-10 00:00:00', NOW(), NOW(), NULL),
('나이키 헤리티지 백팩',        69000, 2, 31, 'https://picsum.photos/seed/nike-backpack/400/533',     '2024-07-15 00:00:00', NOW(), NOW(), NULL),
('무인양품 캔버스 토트백',      25000, 7, 32, 'https://picsum.photos/seed/muji-tote/400/533',         '2024-06-01 00:00:00', NOW(), NOW(), NULL),
('로고 에코백 아이보리',        15000, 1, 33, 'https://picsum.photos/seed/musinsa-eco/400/533',       '2024-04-01 00:00:00', NOW(), NOW(), NULL),
('아디다스 리니어 더플백',      59000, 3, 34, 'https://picsum.photos/seed/adidas-duffle/400/533',     '2024-08-01 00:00:00', NOW(), NOW(), NULL),
('나이키 헤리티지 웨이스트백',   35000, 2, 35, 'https://picsum.photos/seed/nike-waist/400/533',        '2024-07-01 00:00:00', NOW(), NOW(), NULL),
('무인양품 나일론 파우치',      12000, 7, 36, 'https://picsum.photos/seed/muji-pouch/400/533',        '2024-05-01 00:00:00', NOW(), NOW(), NULL),
('노트북 브리프케이스 블랙',    69000, 1, 37, 'https://picsum.photos/seed/musinsa-brief/400/533',     '2024-09-01 00:00:00', NOW(), NOW(), NULL),
('무인양품 하드 캐리어 36L',   129000, 7, 38, 'https://picsum.photos/seed/muji-carrier/400/533',      '2024-03-01 00:00:00', NOW(), NOW(), NULL),
-- 소품
('실버 체인 목걸이',           25000, 1, 40, 'https://picsum.photos/seed/musinsa-necklace/400/533',  '2024-07-01 00:00:00', NOW(), NOW(), NULL),
('베이직 크루삭스 3팩',        12000, 1, 41, 'https://picsum.photos/seed/musinsa-socks/400/533',     '2024-04-01 00:00:00', NOW(), NOW(), NULL),
('보스턴 라운드 안경테',       35000, 1, 42, 'https://picsum.photos/seed/musinsa-glasses/400/533',   '2024-06-15 00:00:00', NOW(), NOW(), NULL),
('미니멀 메탈 워치 실버',      59000, 1, 43, 'https://picsum.photos/seed/musinsa-watch/400/533',     '2024-08-01 00:00:00', NOW(), NOW(), NULL),
('레더 캐주얼 벨트 브라운',     29000, 1, 44, 'https://picsum.photos/seed/musinsa-belt/400/533',      '2024-07-15 00:00:00', NOW(), NOW(), NULL);

-- 5. 쿠폰 (받기 가능한 쿠폰)
INSERT INTO coupon (coupon_name, coupon_code, coupon_type, value, valid_from, valid_to, valid_days, created_at, updated_at, deleted_at) VALUES
('신규 가입 축하 3,000원 할인', '3C9504A7BEF01', 'FIXED_AMOUNT', 3000,  DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 180 DAY), NULL, NOW(), NOW(), NULL),
('여름 시즌 5,000원 할인',      '7D2E18F3CA502', 'FIXED_AMOUNT', 5000,  DATE_SUB(NOW(), INTERVAL 7 DAY),  DATE_ADD(NOW(), INTERVAL 60 DAY),  NULL, NOW(), NOW(), NULL),
('첫 구매 10% 할인',            'A1B904C8DE703', 'PERCENTAGE',   10,    DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY),  7,   NOW(), NOW(), NULL),
('브랜드위크 15% 할인',         'E5F610D2AB804', 'PERCENTAGE',   15,    DATE_SUB(NOW(), INTERVAL 3 DAY),  DATE_ADD(NOW(), INTERVAL 14 DAY),  NULL, NOW(), NOW(), NULL),
('VIP 전용 10,000원 할인',      'B8C723E9F1A05', 'FIXED_AMOUNT', 10000, DATE_SUB(NOW(), INTERVAL 1 DAY),  DATE_ADD(NOW(), INTERVAL 30 DAY),  14,  NOW(), NOW(), NULL);

-- 6. 상품 좋아요 (user id 1~20, product id 1~17)
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

-- 6. like_summary (비정규화 테이블, product_like 건수와 정합성 유지)
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
( 1, 6,  'PRODUCT', NOW(), NOW(), NULL),
( 0, 18, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 19, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 20, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 21, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 22, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 23, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 24, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 25, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 26, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 27, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 28, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 29, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 30, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 31, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 32, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 33, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 34, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 35, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 36, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 37, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 38, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 39, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 40, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 41, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 42, 'PRODUCT', NOW(), NOW(), NULL),
( 0, 43, 'PRODUCT', NOW(), NOW(), NULL);
