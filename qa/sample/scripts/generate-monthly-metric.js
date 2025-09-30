// scripts/generate-monthly-metric.js
// product_metrics_monthly 더미 SQL 생성기
// 사용 예:
// node scripts/generate-monthly-metric.js --months 12 --products 50000 --seed 42 --batch 2000 [--start 2025-09-01] [--truncate 1] [--replace 1]

const fs = require('fs');
const path = require('path');

function parseArgs() {
    const args = process.argv.slice(2);
    const opt = {};
    for (let i = 0; i < args.length; i++) {
        const k = args[i];
        const v = args[i + 1];
        if (k === '--months')   opt.months   = parseInt(v, 10);
        if (k === '--products') opt.products = parseInt(v, 10);
        if (k === '--seed')     opt.seed     = parseInt(v, 10);
        if (k === '--batch')    opt.batch    = parseInt(v, 10);
        if (k === '--start')    opt.startMonth = v;         // YYYY-MM-01 권장
        if (k === '--truncate') opt.truncate = parseInt(v, 10) === 1;
        if (k === '--replace')  opt.replace  = parseInt(v, 10) === 1;
    }
    opt.months   = Number.isFinite(opt.months)   ? opt.months   : 12;
    opt.products = Number.isFinite(opt.products) ? opt.products : 50000;
    opt.seed     = Number.isFinite(opt.seed)     ? opt.seed     : 42;
    opt.batch    = Number.isFinite(opt.batch)    ? opt.batch    : 2000;
    return opt;
}

function seededRand(seed) {
    let s = seed >>> 0;
    return () => {
        s = (s * 1664525 + 1013904223) >>> 0;
        return s / 0xFFFFFFFF;
    };
}

function toDate(str) { const [y,m,d] = str.split('-').map(Number); return new Date(y, m-1, d); }

function firstDayOfMonth(d) {
    const x = new Date(d);
    x.setDate(1);
    x.setHours(0,0,0,0);
    return x;
}

function buildMonthStarts(months, startMonth) {
    const arr = [];
    if (startMonth) {
        let cur = firstDayOfMonth(toDate(startMonth));
        for (let i = 0; i < months; i++) {
            const d = new Date(cur.getFullYear(), cur.getMonth() - i, 1);
            d.setHours(0,0,0,0);
            arr.push(d);
        }
    } else {
        const now = new Date();
        const cur = firstDayOfMonth(now);
        for (let i = 0; i < months; i++) {
            const d = new Date(cur.getFullYear(), cur.getMonth() - i, 1);
            d.setHours(0,0,0,0);
            arr.push(d);
        }
    }
    return arr; // 최신 → 과거
}

function endOfMonth(d) {
    const x = new Date(d.getFullYear(), d.getMonth() + 1, 0);
    x.setHours(23,59,59,0);
    return x;
}

function fmtDate(d) {
    const y = d.getFullYear();
    const m = String(d.getMonth()+1).padStart(2,'0');
    const day = String(d.getDate()).padStart(2,'0');
    return `${y}-${m}-${day}`;
}

function fmtDateTime(d) {
    const date = fmtDate(d);
    const hh = String(d.getHours()).padStart(2,'0');
    const mm = String(d.getMinutes()).padStart(2,'0');
    const ss = String(d.getSeconds()).padStart(2,'0');
    return `${date} ${hh}:${mm}:${ss}`;
}

function randomInMonth(rand, monthStart) {
    const start = new Date(monthStart);
    const end = endOfMonth(monthStart);
    const t = start.getTime() + Math.floor(rand() * (end.getTime() - start.getTime() + 1));
    return new Date(t);
}

function main() {
    const { months, products, seed, batch, startMonth, truncate, replace } = parseArgs();

    const resultsDir = path.join(__dirname, '../results');
    if (!fs.existsSync(resultsDir)) fs.mkdirSync(resultsDir, { recursive: true });
    const out = path.join(resultsDir, 'monthly-metrics.sql');

    const rand = seededRand(seed);
    const monthStarts = buildMonthStarts(months, startMonth);

    // 조회수→주문→좋아요 상관 관계를 월간 스케일로 생성
    const genCounts = (pid, monthIdx) => {
        const baseView = 500 + Math.floor(rand() * 50000); // 월간 조회량 스케일 업
        const trend = Math.max(0.6, 1.15 - monthIdx * 0.03); // 과거로 갈수록 약간 감소
        const view = Math.floor(baseView * trend * (0.7 + rand() * 0.6)); // ±30%

        const cr = 0.005 + rand() * 0.02; // 0.5% ~ 2.5%
        const order = Math.max(0, Math.floor(view * cr));

        const like = Math.max(0, Math.floor(order * (0.3 + rand()*1.2) + view * 0.005 * rand()));
        return { like, order, view };
    };

    let sql = '';
    sql += `-- product_metrics_monthly 더미 데이터 생성\n`;
    sql += `-- created: ${new Date().toISOString()}\n\n`;
    sql += `SET NAMES utf8mb4; SET CHARACTER SET utf8mb4; SET character_set_connection=utf8mb4;\n`;
    sql += `SET autocommit = 0;\nSTART TRANSACTION;\n\n`;

    if (truncate) {
        sql += `-- ⚠️ 전체 비우기 (개발용)\n`;
        sql += `SET FOREIGN_KEY_CHECKS = 0;\n`;
        sql += `TRUNCATE TABLE product_metrics_monthly;\n`;
        sql += `SET FOREIGN_KEY_CHECKS = 1;\n\n`;
    }

    const table = 'product_metrics_monthly';
    const cols = '(ref_product_id, like_count, sale_quantity, view_count, month_start, created_at, updated_at)';
    const insertHead = `INSERT INTO ${table} ${cols} VALUES\n`;

    let buf = [];
    let inserted = 0;

    monthStarts.forEach((mStart, midx) => {
        const mEnd = endOfMonth(mStart);

        for (let pid = 1; pid <= products; pid++) {
            const { like, order, view } = genCounts(pid, midx);

            const created = randomInMonth(rand, mStart);
            const updated = new Date(created);
            const addDays = Math.floor(rand() * 7); // created 이후 0~6일 사이
            updated.setDate(updated.getDate() + addDays);
            const now = new Date();
            if (updated > mEnd) updated.setTime(mEnd.getTime());
            if (updated > now)  updated.setTime(now.getTime());

            buf.push(
                `(${pid}, ${like}, ${order}, ${view}, '${fmtDate(mStart)}', '${fmtDateTime(created)}', '${fmtDateTime(updated)}')`
            );

            if (buf.length >= batch) {
                sql += insertHead + buf.join(',\n') + ';\n';
                if (replace) {
                    sql = sql.replace(/;$/, ` ON DUPLICATE KEY UPDATE
  like_count=VALUES(like_count),
  sale_quantity=VALUES(sale_quantity),
  view_count=VALUES(view_count),
  created_at=LEAST(${table}.created_at, VALUES(created_at)),
  updated_at=GREATEST(${table}.updated_at, VALUES(updated_at));\n`);
                }
                buf = [];
            }
            inserted++;
        }
    });

    if (buf.length) {
        sql += insertHead + buf.join(',\n') + ';\n';
        if (replace) {
            sql = sql.replace(/;$/, ` ON DUPLICATE KEY UPDATE
  like_count=VALUES(like_count),
  sale_quantity=VALUES(sale_quantity),
  view_count=VALUES(view_count),
  created_at=LEAST(${table}.created_at, VALUES(created_at)),
  updated_at=GREATEST(${table}.updated_at, VALUES(updated_at));\n`);
        }
    }

    sql += `\nCOMMIT;\nSET autocommit = 1;\n`;

    fs.writeFileSync(out, sql, 'utf8');
    console.log(`생성 완료: ${out}`);
    console.log(`총 행 수: ${inserted.toLocaleString()} (months=${months}, products=${products}) truncate=${!!truncate} replace=${!!replace}`);
}

main();
