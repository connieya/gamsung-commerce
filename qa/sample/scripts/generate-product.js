// generate-product.js
// 100만 개의 상품 데이터를 생성하여 SQL 파일로 저장하는 스크립트입니다.

const fs = require('fs');
const path = require('path');

// --- 파일 경로 설정 ---
// 현재 스크립트 위치(__dirname)를 기준으로 파일 경로를 설정합니다.
// 'scripts' 폴더에서 상위 폴더(..)로 이동하여 'data'와 'results' 폴더에 접근합니다.
const adjectivesPath = path.join(__dirname, '../data/adjectives.csv');
const productNamesPath = path.join(__dirname, '../data/product-names.csv');
const brandsPath = path.join(__dirname, '../data/brand-sample-data.csv');
const resultsDir = path.join(__dirname, '../results');
const outputSqlPath = path.join(resultsDir, 'products-data.sql');

// 최종 생성할 상품 데이터 개수
const TOTAL_PRODUCTS = 500000;

// 결과 디렉터리가 없으면 생성
if (!fs.existsSync(resultsDir)) {
    fs.mkdirSync(resultsDir, { recursive: true });
}

console.log('--- 100만 개 상품 데이터 SQL 문 생성을 시작합니다 ---');

// --- 1. CSV 파일 데이터 로드 및 파싱 ---

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

const adjectives = readCsv(adjectivesPath);
const productNames = readCsv(productNamesPath);

// 브랜드 CSV 파일 파싱
const brandLines = readCsv(brandsPath);
// 브랜드 ID는 1부터 시작하는 순차적인 값이라고 가정하고 배열을 생성합니다.
const brandIds = Array.from({ length: brandLines.length }, (_, i) => i + 1);

if (adjectives.length === 0 || productNames.length === 0 || brandIds.length === 0) {
    console.error('에러: 필수 CSV 파일에 데이터가 비어 있습니다.');
    process.exit(1);
}

// --- 2. SQL INSERT 문 생성 시작 ---

let sqlContent = `-- product 테이블에 100만 개 더미 데이터 삽입\n`;
sqlContent += `-- 생성 시간: ${new Date().toISOString()}\n\n`;
sqlContent += '-- 한글 지원을 위한 문자셋 설정\n';
sqlContent += 'SET NAMES utf8mb4;\n';
sqlContent += 'SET CHARACTER SET utf8mb4;\n';
sqlContent += 'SET character_set_connection=utf8mb4;\n\n';

// 기존 데이터 정리 (외래 키 문제 해결을 위해 SET FOREIGN_KEY_CHECKS를 0으로 설정해야 합니다)
sqlContent += 'SET FOREIGN_KEY_CHECKS = 0;\n';
sqlContent += `TRUNCATE TABLE product_like;\n`;
sqlContent += `TRUNCATE TABLE product;\n`;
sqlContent += `TRUNCATE TABLE brand;\n`;
sqlContent += `TRUNCATE TABLE users;\n\n`;
sqlContent += 'SET FOREIGN_KEY_CHECKS = 1;\n\n';


// INSERT 문 시작
sqlContent += `INSERT INTO product (name, price, ref_brand_id, released_at, created_at, updated_at) VALUES\n`;

// --- 3. 100만 개 데이터 조합 및 VALUES 구문 생성 ---

const values = [];
const brandCount = brandIds.length;
const adjectiveCount = adjectives.length;
const productNameCount = productNames.length;

// 최근 1년 이내의 날짜를 생성하기 위한 기준점
const oneYearAgo = new Date();
oneYearAgo.setFullYear(oneYearAgo.getFullYear() - 1);

for (let i = 0; i < TOTAL_PRODUCTS; i++) {
    // 랜덤 인덱스 선택
    const randomAdjectiveIndex = Math.floor(Math.random() * adjectiveCount);
    const randomProductNameIndex = Math.floor(Math.random() * productNameCount);
    const randomBrandIndex = Math.floor(Math.random() * brandCount / 10);

    // 데이터 조합 - 이름 뒤에 고유 식별자(인덱스 i + 1) 추가
    const name = `${adjectives[randomAdjectiveIndex].trim()} ${productNames[randomProductNameIndex].trim()} #${i + 1}`;
    const price = Math.floor(Math.random() * (10000000 - 1000 + 1)) + 1000;
    const brandId = brandIds[randomBrandIndex];

    // 최근 1년 이내의 랜덤 날짜 생성
    const randomDate = new Date(oneYearAgo.getTime() + Math.random() * (new Date().getTime() - oneYearAgo.getTime()));
    const releasedAt = randomDate.toISOString().slice(0, 10); // 'YYYY-MM-DD' 형식으로 변환

    // SQL 문에 들어갈 안전한 문자열로 변환 (' -> '')
    const safeName = name.replace(/'/g, "''");

    // VALUES 구문 포맷
    values.push(`('${safeName}', ${price}, ${brandId}, '${releasedAt}', NOW(), NOW())`);
}

sqlContent += values.join(',\n') + ';\n';

console.log(`총 ${TOTAL_PRODUCTS}개의 상품 데이터 SQL 문이 생성되었습니다.`);

// --- 4. SQL 파일 저장 ---

fs.writeFileSync(outputSqlPath, sqlContent);

console.log(`SQL 파일이 생성되었습니다: ${outputSqlPath}`);
console.log('--- 상품 데이터 SQL 문 생성 완료 ---');
