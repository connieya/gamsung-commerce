// generate-users.js

const fs = require('fs');
const path = require('path');

// user-sample.csv 파일 경로
const userSamplePath = path.join(__dirname, '../data/user-sample.csv');
// SQL 결과 파일 경로
const resultSqlPath = path.join(__dirname, '../results', 'users-data.sql');
// 결과 디렉토리 생성
if (!fs.existsSync(path.join(__dirname, 'results'))) {
    fs.mkdirSync(path.join(__dirname, 'results'));
}

console.log('사용자 샘플 데이터 SQL 문 생성을 시작합니다...');

// 1. 사용자 샘플 CSV 데이터 로드
const userCsv = fs.readFileSync(userSamplePath, 'utf8');
const userLines = userCsv.split('\n').slice(1); // 헤더 제거
const users = userLines
    .filter(line => line.trim()) // 빈 줄 제거
    .map(line => {
        // CSV 데이터 파싱
        const [user_id, email, gender, birth_date] = line.split(',');
        return {
            userId: user_id.trim(),
            email: email.trim(),
            gender: gender.trim(),
            birthDate: birth_date.trim()
        };
    });

// 2. SQL INSERT 문 생성
let sqlContent = `-- users 테이블에 user-sample.csv 데이터 삽입\n`;
sqlContent += `-- 생성 시간: ${new Date().toISOString()}\n\n`;
sqlContent += '-- 한글 지원을 위한 문자셋 설정\n';
sqlContent += 'SET NAMES utf8mb4;\n';
sqlContent += 'SET CHARACTER SET utf8mb4;\n';
sqlContent += 'SET character_set_connection=utf8mb4;\n\n';

// 기존 데이터 정리
sqlContent += `TRUNCATE TABLE users;\n\n`;

// INSERT 문 시작
sqlContent += `INSERT INTO users (user_name, email, gender, birth_date, created_at, updated_at) VALUES\n`;

// 3. CSV 데이터로 VALUES 구문 생성
const values = users.map(user => {
    // SQL 문에 들어갈 안전한 문자열로 변환 (' -> '')
    const safeUserName = user.userId.replace(/'/g, "''");
    const safeEmail = user.email.replace(/'/g, "''");

    // 성별 매핑 (Male: 1, Female: 2)
    const genderString = user.gender.toUpperCase();

    return `('${safeUserName}', '${safeEmail}', ${genderString}, '${user.birthDate}', NOW(), NOW())`;
});

sqlContent += values.join(',\n') + ';\n';

console.log(`총 ${users.length}개의 사용자 데이터 SQL 문이 생성되었습니다.`);

// 4. SQL 파일 저장
fs.writeFileSync(resultSqlPath, sqlContent);

console.log(`SQL 파일이 생성되었습니다: ${resultSqlPath}`);
