// scripts/generate-weekly-metric.js
// product_metrics_weekly 더미 SQL 생성기
// 사용법:
// node scripts/generate-weekly-metric.js --weeks 12 --products 50000 --seed 42 --batch 2000 [--start 2025-09-15]

const fs = require('fs');
const path = require('path');

function parseArgs() {
    const args = process.argv.slice(2);
    const opt = {};
    for (let i = 0; i < args.length; i += 2) {
        const k = args[i];
        const v = args[i + 1];
        if (!v || v.startsWith('--')) { i--; continue; }
        if (k === '--weeks') opt.weeks = parseInt(v, 10);
        if (k === '--products') opt.products = parseInt(v, 10);
        if (k === '--seed') opt.seed = parseInt(v, 10);
        if (k === '--batch') opt.batch = parseInt(v, 10);
        if (k === '--start') opt.startMonday = v;
    }
    opt.weeks = Number.isFinite(opt.weeks) ? opt.weeks : 12;
    opt.products = Number.isFinite(opt.products) ? opt.products : 50000;
    opt.seed = Number.isFinite(opt.seed) ? opt.seed : 42;
    opt.batch = Number.isFinite(opt.batch) ? opt.batch : 2000;
    return opt;
}

// 간단한 선형합동 난수 (재현성 보장)
function seededRand(seed) {
    let s = seed >>> 0;
    return () => {
        s = (s * 1664525 + 1013904223) >>> 0;
        return s / 0xFFFFFFFF;
    };
}

// YYYY-MM-DD 문자열 → Date(로컬)
function toDate(str) {
    const [y, m, d] = str.split('-').map(Number);
    return new Date(y, m - 1, d);
}

// 오늘 기준 그 주 월요일
function mondayOf(date) {
    const day = date.getDay(); // 0: Sun, 1: Mon, ...
    const diff = (day === 0 ? -6 : 1 - day);
    const md = new Date(date);
    md.setDate(date.getDate() + diff);
    md.setHours(0, 0, 0, 0);
    return md;
}

// 최근 N주 월요일 배열 만들기 (startMonday가 있으면 거기부터 뒤로 N주)
function buildMondays(weeks, startMonday) {
    const arr = [];
    if (startMonday) {
        let cur = toDate(startMonday);
        cur.setHours(0, 0, 0, 0);
        // 입력이 월요일인지 보정
        cur = mondayOf(cur);
        for (let i = 0; i < weeks; i++) {
            const d = new Date(cur);
            d.setDate(cur.getDate() - 7 * i);
            arr.push(d);
        }
    } else {
        const now = new Date();
        const thisMonday = mondayOf(now);
        for (let i = 0; i < weeks; i++) {
            const d = new Date(thisMonday);
            d.setDate(thisMonday.getDate() - 7 * i);
            arr.push(d);
        }
    }
    // 최신 → 과거 순서가 되도록 유지 (필요 시 reverse 가능)
    return arr;
}

function fmt(date) {
    // YYYY-MM-DD
    const y = date.getFullYear();
    const m = `${date.getMonth() + 1}`.padStart(2, '0');
    const d = `${date.getDate()}`.padStart(2, '0');
    return `${y}-${m}-${d}`;
}

function main() {
    const { weeks, products, seed, batch, startMonday } = parseArgs();

    const resultsDir = path.join(__dirname, '../results');
    if (!fs.existsSync(resultsDir)) fs.mkdirSync(resultsDir, { recursive: true });
    const out = path.join(resultsDir, 'weekly-metrics.sql');

    const rand = seededRand(seed);
    const mondays = buildMondays(weeks, startMonday);

    // 값 생성 헬퍼: 조회수 → 주문 → 좋아요 상관관계 비슷하게
    const genCounts = (productId, weekIdx) => {
        // 제품/주차별 base를 만들어서 편차를 주자
        const baseView = 100 + Math.floor(rand() * 10000); // 100~10100
        const trend = Math.max(0.6, 1.2 - weekIdx * 0.03); // 과거로 갈수록 약간 감소
        const view = Math.floor(baseView * trend * (0.7 + rand() * 0.6)); // ±30%

        // 조회수→주문 전환율 0.2% ~ 2%
        const cr = 0.002 + rand() * 0.018;
        const order = Math.max(0, Math.floor(view * cr));

        // 주문당 좋아요 0.5~2개 + 조회에 소량 가산
        const like = Math.max(0, Math.floor(order * (0.5 + rand() * 1.5) + view * 0.01 * rand()));

        return { like, order, view };
    };

    let sql = '';
    sql += `-- product_metrics_weekly 더미 데이터 생성\n`;
    sql += `-- created: ${new Date().toISOString()}\n\n`;
    sql += `SET NAMES utf8mb4; SET CHARACTER SET utf8mb4; SET character_set_connection=utf8mb4;\n`;
    sql += `SET autocommit = 0;\nSTART TRANSACTION;\n\n`;

    // 기존 데이터 정리
    sql += `TRUNCATE TABLE product_metrics_weekly;\n\n`;

    // 필요하면 초기화 (주의: 실제 운영DB에서는 주석 유지 권장)
    // sql += `TRUNCATE TABLE product_metrics_weekly;\n\n`;

    const table = 'product_metrics_weekly';
    const cols = '(ref_product_id, like_count, sale_quantity, view_count, week_start, created_at, updated_at)';
    const insertHead = `INSERT INTO ${table} ${cols} VALUES\n`;

    let buf = [];
    let inserted = 0;

    // 최신 주 → 과거 주 순서로 생성
    mondays.forEach((monday, widx) => {
        for (let pid = 1; pid <= products; pid++) {
            const { like, order, view } = genCounts(pid, widx);
            // unique key (week_start, ref_product_id) 보장 위해 한 row 당 한 조합
            buf.push(
                `(${pid}, ${like}, ${order}, ${view}, '${fmt(monday)}', NOW(), NOW())`
            );

            if (buf.length >= batch) {
                sql += insertHead + buf.join(',\n') + ';\n';
                buf = [];
            }
            inserted++;
        }
    });

    if (buf.length) {
        sql += insertHead + buf.join(',\n') + ';\n';
    }

    sql += `\nCOMMIT;\nSET autocommit = 1;\n`;

    fs.writeFileSync(out, sql, 'utf8');
    console.log(`생성 완료: ${out}`);
    console.log(`총 행 수: ${inserted.toLocaleString()} (weeks=${weeks}, products=${products})`);
}

main();
