// generate-like-summary.js
// `product_like` 테이블의 데이터를 기반으로 `like_summary` 테이블의 데이터를 생성합니다.

const fs = require('fs');
const path = require('path');

// --- 파일 경로 설정 ---
const resultsDir = path.join(__dirname, '../results');
const outputSqlPath = path.join(resultsDir, 'like_summary.sql');

// --- 데이터 생성 상수 ---
// 기존 `generate-all-data.js` 스크립트와 동일한 상수를 사용
const TOTAL_PRODUCTS = 500000;
const TOTAL_LIKES = 1000000;

// --- 시작 로직 ---
if (!fs.existsSync(resultsDir)) {
    fs.mkdirSync(resultsDir, { recursive: true });
}
console.log('--- `like_summary` 테이블 더미 데이터 SQL 문 생성을 시작합니다 ---');

// --- 상품 좋아요 데이터 집계 ---
console.log('상품 좋아요 데이터를 집계 중입니다. 잠시만 기다려주세요...');

const likeCounts = new Map(); // 상품 ID별 좋아요 수를 저장할 맵
const uniqueLikes = new Set(); // 중복된 좋아요 조합을 저장할 Set

for (let i = 0; i < TOTAL_LIKES; i++) {
    const randomUserId = Math.floor(Math.random() * 1000) + 1; // 원본 스크립트의 userCount는 1000으로 가정
    const randomProductId = Math.floor(Math.random() * TOTAL_PRODUCTS) + 1;
    const uniqueKey = `${randomUserId}-${randomProductId}`;

    // 원본 스크립트와 동일하게 중복 좋아요를 방지하는 로직
    if (!uniqueLikes.has(uniqueKey)) {
        uniqueLikes.add(uniqueKey);
        // 좋아요가 유니크하면 해당 상품의 좋아요 수를 증가
        likeCounts.set(randomProductId, (likeCounts.get(randomProductId) || 0) + 1);
    }
}
console.log(`총 ${uniqueLikes.size}개의 고유한 좋아요 데이터를 기반으로 집계가 완료되었습니다.`);

// --- SQL 파일 시작 부분 생성 ---
let sqlContent = `-- product_like 데이터를 기반으로 like_summary 데이터 삽입\n`;
sqlContent += `-- 생성 시간: ${new Date().toISOString()}\n\n`;
sqlContent += '-- 한글 지원을 위한 문자셋 설정\n';
sqlContent += 'SET NAMES utf8mb4;\n';
sqlContent += 'SET CHARACTER SET utf8mb4;\n';
sqlContent += 'SET character_set_connection=utf8mb4;\n\n';

// 외래 키 제약 조건을 일시적으로 비활성화하고 테이블을 정리합니다.
sqlContent += 'SET FOREIGN_KEY_CHECKS = 0;\n';
sqlContent += `TRUNCATE TABLE like_summary;\n`;
sqlContent += 'SET FOREIGN_KEY_CHECKS = 1;\n\n';

// --- `like_summary` 데이터 삽입 ---
sqlContent += `-- 1. like_summary 데이터 삽입 (target_type='PRODUCT')\n`;
sqlContent += `INSERT INTO like_summary (like_count, target_id, target_type, created_at, updated_at) VALUES\n`;

const likeSummaryValues = [];
likeCounts.forEach((count, productId) => {
    likeSummaryValues.push(`(${count}, ${productId}, 'PRODUCT', NOW(), NOW())`);
});

sqlContent += likeSummaryValues.join(',\n') + ';\n';
console.log(`총 ${likeSummaryValues.length}개의 like_summary 데이터 SQL 문이 생성되었습니다.`);

// --- 최종 파일 저장 ---
fs.writeFileSync(outputSqlPath, sqlContent);
console.log(`SQL 파일이 생성되었습니다: ${outputSqlPath}`);
console.log('--- `like_summary` 데이터 SQL 문 생성 완료 ---');
