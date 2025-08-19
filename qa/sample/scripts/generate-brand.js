// generate-brand.js

const fs = require('fs');
const path = require('path');

// 현재 스크립트 위치(__dirname)를 기준으로 파일 경로 설정
// 'scripts' 폴더에서 상위 폴더(..)로 이동하여 'data'와 'results' 폴더에 접근합니다.
const brandSamplePath = path.join(__dirname, '../data', 'brand-sample-data.csv');
const resultsDir = path.join(__dirname, '../results');
const resultSqlPath = path.join(resultsDir, 'brands-data.sql');

// 결과 디렉터리가 없으면 생성
if (!fs.existsSync(resultsDir)) {
    fs.mkdirSync(resultsDir, { recursive: true });
}

console.log('브랜드 샘플 데이터 SQL 문 생성을 시작합니다...');

// 1. 브랜드 샘플 CSV 데이터 로드
let brandCsv = '';
try {
    brandCsv = fs.readFileSync(brandSamplePath, 'utf8');
} catch (error) {
    console.error(`에러: brand-sample-data.csv 파일을 찾을 수 없습니다. 경로를 확인하세요: ${brandSamplePath}`);
    process.exit(1);
}

const brandLines = brandCsv.split('\n').slice(1); // 헤더 제거
const brands = brandLines
    .filter(line => line.trim()) // 빈 줄 제거
    .map(line => {
        let name = '';
        let description = '';

        // 정규식을 사용하여 따옴표로 감싸인 필드와 그렇지 않은 필드를 분리
        // 이 정규식은 쉼표를 기준으로 필드를 분리하되, 따옴표 안의 쉼표는 무시합니다.
        const fields = line.match(/(".*?"|[^,]+)/g);

        if (fields && fields.length >= 2) {
            // 필드의 양 끝에 있는 따옴표와 공백을 제거
            name = fields[0].trim().replace(/^"|"$/g, '');
            description = fields[1].trim().replace(/^"|"$/g, '');
        } else if (fields && fields.length === 1) {
            // 설명이 없는 경우
            name = fields[0].₩₩rim().replace(/^"|"$/g, '');
        }

        return { name, description };
    });

// 2. SQL INSERT 문 생성
let sqlContent = `-- brand 테이블에 brand-sample-data.csv 데이터 삽입\n`;
sqlContent += `-- 생성 시간: ${new Date().toISOString()}\n\n`;
sqlContent += '-- 한글 지원을 위한 문자셋 설정\n';
sqlContent += 'SET NAMES utf8mb4;\n';
sqlContent += 'SET CHARACTER SET utf8mb4;\n';
sqlContent += 'SET character_set_connection=utf8mb4;\n\n';

// 기존 데이터 정리
sqlContent += 'SET FOREIGN_KEY_CHECKS = 0;\n';
sqlContent += `TRUNCATE TABLE product_like;\n`;
sqlContent += `TRUNCATE TABLE product;\n`;
sqlContent += `TRUNCATE TABLE brand;\n`;
sqlContent += `TRUNCATE TABLE users;\n\n`;
sqlContent += 'SET FOREIGN_KEY_CHECKS = 1;\n\n';

// INSERT 문 시작
sqlContent += `INSERT INTO brand (name, description, created_at, updated_at) VALUES\n`;

// 3. CSV 데이터로 VALUES 구문 생성
const values = brands.map(brand => {
    // SQL 문에 들어갈 안전한 문자열로 변환 (' -> '')
    const safeName = brand.name.replace(/'/g, "''");
    const safeDescription = brand.description.replace(/'/g, "''");

    return `('${safeName}', '${safeDescription}', NOW(), NOW())`;
});

sqlContent += values.join(',\n') + ';\n';

console.log(`총 ${brands.length}개의 브랜드 데이터 SQL 문이 생성되었습니다.`);

// 4. SQL 파일 저장
fs.writeFileSync(resultSqlPath, sqlContent);

console.log(`SQL 파일이 생성되었습니다: ${resultSqlPath}`);
