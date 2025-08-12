// generate-all-data.js
// 모든 테이블(users, brand, product, product_like)의 더미 데이터를 생성하여
// 하나의 SQL 파일로 저장하는 스크립트입니다.

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
const TOTAL_LIKES = 10000; // 상품 좋아요 데이터 개수

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

// --- SQL 파일 시작 부분 생성 ---
let sqlContent = `-- 모든 테이블(users, brand, product, product_like) 데이터 삽입\n`;
sqlContent += `-- 생성 시간: ${new Date().toISOString()}\n\n`;
sqlContent += '-- 한글 지원을 위한 문자셋 설정\n';
sqlContent += 'SET NAMES utf8mb4;\n';
sqlContent += 'SET CHARACTER SET utf8mb4;\n';
sqlContent += 'SET character_set_connection=utf8mb4;\n\n';

// 외래 키 제약 조건을 일시적으로 비활성화하고 테이블을 정리합니다.
sqlContent += 'SET FOREIGN_KEY_CHECKS = 0;\n';
sqlContent += `TRUNCATE TABLE product_like;\n`;
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

// --- 사용자 데이터 삽입 ---
sqlContent += `-- 2. 사용자 데이터 삽입\n`;
sqlContent += `INSERT INTO users (user_id, email, gender, birth_date, created_at, updated_at) VALUES\n`;
const userValues = userLines.map(line => {
    const [userId, email, gender, birthDate] = line.split(',');
    const safeUserName = userId.trim().replace(/'/g, "''");
    const safeEmail = email.trim().replace(/'/g, "''");

    // 성별 매핑: Male: 1, Female: 2
    const genderId = gender.trim().toUpperCase() === 'FEMALE' ? 2 : 1;

    return `('${safeUserName}', '${safeEmail}', ${genderId}, '${birthDate.trim()}', NOW(), NOW())`;
});
sqlContent += userValues.join(',\n') + ';\n\n';
console.log(`총 ${userLines.length}개의 사용자 데이터 SQL 문이 생성되었습니다.`);

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

// --- 상품 좋아요 데이터 삽입 ---
sqlContent += `-- 4. 상품 좋아요 데이터 삽입\n`;
sqlContent += `INSERT INTO product_like (ref_user_id, ref_product_id, created_at, updated_at) VALUES\n`;
const likeValues = [];
const userCount = userLines.length;

for (let i = 0; i < TOTAL_LIKES; i++) {
    const randomUserId = Math.floor(Math.random() * userCount) + 1; // 사용자 ID는 1부터 시작
    const randomProductId = Math.floor(Math.random() * TOTAL_PRODUCTS) + 1; // 상품 ID는 1부터 시작
    likeValues.push(`(${randomUserId}, ${randomProductId}, NOW(), NOW())`);
}
sqlContent += likeValues.join(',\n') + ';\n';
console.log(`총 ${TOTAL_LIKES}개의 상품 좋아요 데이터 SQL 문이 생성되었습니다.`);

// --- 최종 파일 저장 ---
fs.writeFileSync(outputSqlPath, sqlContent);
console.log(`SQL 파일이 생성되었습니다: ${outputSqlPath}`);
console.log('--- 모든 테이블 데이터 SQL 문 생성 완료 ---');
