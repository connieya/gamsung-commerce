// generate-all-data.js
// 모든 테이블(users, brand, product, product_like, like_summary)의 더미 데이터를 생성하여
// 하나의 SQL 파일로 저장하는 스크립트입니다.
// 좋아요 분포: 멱법칙(Zipf). 인기 상품(낮은 product_id)에 좋아요 집중 → 비정규화 전/후 부하 차이 입증.

const fs = require('fs');
const path = require('path');

// --- 파일 경로 설정 ---
const resultsDir = path.join(__dirname, '../results');
const outputSqlPath = path.join(resultsDir, 'all-data.sql');

// CSV 파일 경로
const userSamplePath = path.join(__dirname, '../data/user-sample.csv');
const brandSamplePath = path.join(__dirname, '../data/brand-sample-data.csv');
const adjectivesPath = path.join(__dirname, '../data/adjectives.csv');
const productNamesPath = path.join(__dirname, '../data/product-names.csv');

// --- 데이터 생성 상수 ---
const TOTAL_PRODUCTS = 500000;
const TOTAL_LIKES = 5000000;   // 상품당 평균 10개, 비정규화 전/후 부하 차이 입증용
const TOTAL_USERS = 50000;     // 좋아요 조합 다양성 확보
const ZIPF_S = 1;              // Zipf 지수 (1 = 전형적 멱법칙)

// --- 시작 로직 ---
if (!fs.existsSync(resultsDir)) {
    fs.mkdirSync(resultsDir, { recursive: true });
}
console.log('--- 모든 테이블 더미 데이터 SQL 문 생성을 시작합니다 ---');

// --- CSV 파일 데이터 로드 및 파싱 ---

/**
 * 주어진 파일 경로에서 CSV 데이터를 읽고 파싱하는 함수
 * @param {string} filePath - 읽을 CSV 파일 경로
 * @returns {string[]} - 헤더를 제외한 데이터 배열
 */
const readCsv = (filePath) => {
    try {
        const content = fs.readFileSync(filePath, 'utf8');
        return content.split('\n').slice(1).filter(line => line.trim());
    } catch (error) {
        console.error(`에러: ${filePath} 파일을 찾을 수 없거나 읽을 수 없습니다.`);
        console.error(error);
        process.exit(1);
    }
};

const userLines = readCsv(userSamplePath);
const brandLines = readCsv(brandSamplePath);
const adjectives = readCsv(adjectivesPath);
const productNames = readCsv(productNamesPath);

if (userLines.length === 0 || brandLines.length === 0 || adjectives.length === 0 || productNames.length === 0) {
    console.error('에러: 필수 CSV 파일 중 데이터가 비어있는 파일이 있습니다.');
    process.exit(1);
}

// --- Zipf(멱법칙) 누적 확률 테이블: rank k 확률 ∝ 1/k^s ---
console.log('Zipf 누적 분포 계산 중...');
const zipfCumulative = [0];
let zipfSum = 0;
for (let k = 1; k <= TOTAL_PRODUCTS; k++) {
    zipfSum += 1 / Math.pow(k, ZIPF_S);
    zipfCumulative.push(zipfSum);
}
const zipfTotal = zipfSum;

/** Zipf 분포로 상품 rank(1-based) 샘플링. rank 1 = 가장 인기 상품 */
function sampleZipfProductId() {
    const u = Math.random() * zipfTotal;
    let lo = 1, hi = TOTAL_PRODUCTS;
    while (lo < hi) {
        const mid = (lo + hi) >> 1;
        if (zipfCumulative[mid] < u) lo = mid + 1;
        else hi = mid;
    }
    return lo;
}

// --- SQL 파일 시작 부분 생성 ---
let sqlContent = `-- 모든 테이블(users, brand, product, product_like, like_summary) 데이터 삽입 (Zipf 분포)\n`;
sqlContent += `-- 생성 시간: ${new Date().toISOString()}\n\n`;
sqlContent += '-- 한글 지원을 위한 문자셋 설정\n';
sqlContent += 'SET NAMES utf8mb4;\n';
sqlContent += 'SET CHARACTER SET utf8mb4;\n';
sqlContent += 'SET character_set_connection=utf8mb4;\n\n';

// 외래 키 제약 조건을 일시적으로 비활성화하고 테이블을 정리합니다.
sqlContent += 'SET FOREIGN_KEY_CHECKS = 0;\n';
sqlContent += `TRUNCATE TABLE product_like;\n`;
sqlContent += `TRUNCATE TABLE like_summary;\n`;
sqlContent += `TRUNCATE TABLE product;\n`;
sqlContent += `TRUNCATE TABLE brand;\n`;
sqlContent += `TRUNCATE TABLE users;\n`;
sqlContent += 'SET FOREIGN_KEY_CHECKS = 1;\n\n';

// --- 브랜드 데이터 삽입 ---
sqlContent += `-- 1. 브랜드 데이터 삽입\n`;
sqlContent += `INSERT INTO brand (name, description, created_at, updated_at) VALUES\n`;
const brandValues = brandLines.map(line => {
    const fields = line.match(/(".*?"|[^,]+)/g);
    let name = fields && fields[0] ? fields[0].trim().replace(/^"|"$/g, '') : '';
    let description = fields && fields[1] ? fields[1].trim().replace(/^"|"$/g, '') : '';

    const safeName = name.replace(/'/g, "''");
    const safeDescription = description.replace(/'/g, "''");
    return `('${safeName}', '${safeDescription}', NOW(), NOW())`;
});
sqlContent += brandValues.join(',\n') + ';\n\n';
console.log(`총 ${brandLines.length}개의 브랜드 데이터 SQL 문이 생성되었습니다.`);

// --- 사용자 데이터 삽입 (TOTAL_USERS명, CSV 행 반복·user_id/email 유니크) ---
sqlContent += `-- 2. 사용자 데이터 삽입\n`;
sqlContent += `INSERT INTO users (user_id, email, gender, birth_date, created_at, updated_at) VALUES\n`;
const userValues = [];
for (let i = 0; i < TOTAL_USERS; i++) {
    const line = userLines[i % userLines.length];
    const parts = line.split(',');
    const gender = parts[2] ? parts[2].trim() : 'Male';
    const birthDate = parts[3] ? parts[3].trim() : '1990-01-01';
    const genderId = gender.toUpperCase() === 'FEMALE' ? 2 : 1;
    const user_id = `user_${i + 1}`;
    const email = `user${i + 1}@qa.loadtest`;
    userValues.push(`('${user_id}', '${email}', ${genderId}, '${birthDate}', NOW(), NOW())`);
}
sqlContent += userValues.join(',\n') + ';\n\n';
console.log(`총 ${TOTAL_USERS}개의 사용자 데이터 SQL 문이 생성되었습니다.`);

// --- 상품 데이터 삽입 ---
sqlContent += `-- 3. 상품 데이터 삽입\n`;
sqlContent += `INSERT INTO product (name, price, ref_brand_id, released_at, created_at, updated_at) VALUES\n`;
const productValues = [];
const brandCount = brandLines.length;
const adjectiveCount = adjectives.length;
const productNameCount = productNames.length;
const oneYearAgo = new Date();
oneYearAgo.setFullYear(oneYearAgo.getFullYear() - 1);

for (let i = 0; i < TOTAL_PRODUCTS; i++) {
    const randomAdjectiveIndex = Math.floor(Math.random() * adjectiveCount);
    const randomProductNameIndex = Math.floor(Math.random() * productNameCount);
    const randomBrandId = Math.floor(Math.random() * brandCount) + 1; // 브랜드 ID는 1부터 시작

    const name = `${adjectives[randomAdjectiveIndex].trim()} ${productNames[randomProductNameIndex].trim()} #${i + 1}`;
    const price = Math.floor(Math.random() * (10000000 - 1000 + 1)) + 1000;

    const randomDate = new Date(oneYearAgo.getTime() + Math.random() * (new Date().getTime() - oneYearAgo.getTime()));
    const releasedAt = randomDate.toISOString().slice(0, 10);

    const safeName = name.replace(/'/g, "''");

    productValues.push(`('${safeName}', ${price}, ${randomBrandId}, '${releasedAt}', NOW(), NOW())`);
}

sqlContent += productValues.join(',\n') + ';\n\n';
console.log(`총 ${TOTAL_PRODUCTS}개의 상품 데이터 SQL 문이 생성되었습니다.`);

// --- 상품 좋아요 데이터 삽입 (Zipf: 인기 상품에 좋아요 집중) ---
sqlContent += `-- 4. 상품 좋아요 데이터 삽입 (Zipf 분포)\n`;
sqlContent += `INSERT INTO product_like (ref_user_id, ref_product_id, created_at, updated_at) VALUES\n`;
const likeValues = [];
const uniqueLikes = new Set();
let attempts = 0;
const maxAttempts = TOTAL_LIKES * 20; // 중복 시 리샘플링 상한

while (likeValues.length < TOTAL_LIKES && attempts < maxAttempts) {
    const randomUserId = Math.floor(Math.random() * TOTAL_USERS) + 1;
    const productId = sampleZipfProductId(); // Zipf로 인기 상품 위주
    const uniqueKey = `${randomUserId}-${productId}`;
    if (!uniqueLikes.has(uniqueKey)) {
        uniqueLikes.add(uniqueKey);
        likeValues.push(`(${randomUserId}, ${productId}, NOW(), NOW())`);
    }
    attempts++;
}
if (likeValues.length < TOTAL_LIKES) {
    console.warn(`경고: 유니크 조합 한계로 좋아요 ${likeValues.length}건만 생성됨 (목표 ${TOTAL_LIKES}). users/상품 조합을 늘려보세요.`);
}
sqlContent += likeValues.join(',\n') + ';\n\n';
console.log(`총 ${likeValues.length}개의 상품 좋아요 데이터 SQL 문이 생성되었습니다. (Zipf 분포)`);

// --- 5. like_summary (동일 product_like 기준 집계, 비정규화 비교용) ---
const likeCountByProduct = new Map();
for (const row of likeValues) {
    const m = row.match(/\((\d+),\s*(\d+),/);
    if (m) {
        const productId = parseInt(m[2], 10);
        likeCountByProduct.set(productId, (likeCountByProduct.get(productId) || 0) + 1);
    }
}
sqlContent += `-- 5. like_summary (product_like와 동일 데이터 집계)\n`;
sqlContent += `INSERT INTO like_summary (like_count, target_id, target_type, created_at, updated_at) VALUES\n`;
const likeSummaryValues = [];
for (let productId = 1; productId <= TOTAL_PRODUCTS; productId++) {
    const count = likeCountByProduct.get(productId) || 0;
    likeSummaryValues.push(`(${count}, ${productId}, 'PRODUCT', NOW(), NOW())`);
}
sqlContent += likeSummaryValues.join(',\n') + ';\n';
console.log(`총 ${likeSummaryValues.length}개의 like_summary 행이 생성되었습니다.`);

// --- 최종 파일 저장 ---
fs.writeFileSync(outputSqlPath, sqlContent);
console.log(`SQL 파일이 생성되었습니다: ${outputSqlPath}`);
console.log('--- 모든 테이블 데이터 SQL 문 생성 완료 ---');
