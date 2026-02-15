import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * 시나리오: 상품 목록 API (비정규화) — LikeSummary 테이블 조인, 캐시 미적용
 * GET /api/v1/products/denormalized/no-brand?page=0&size=5&productSort={SORT_TYPE}
 * product-list.js와 동일한 SORT_TYPE(기본 DENORMALIZED_LIKES_DESC)로 비교용
 */
export const options = {
    vus: 10,
    duration: '60s',
};

function hasValidBody(r) {
    if (r.status !== 200) return false;
    try {
        const body = r.json();
        return body.data && body.data.items && Array.isArray(body.data.items) && body.data.items.length === 5;
    } catch (_) {
        return false;
    }
}

export default function () {
    const specificSort = __ENV.SORT_TYPE || 'DENORMALIZED_LIKES_DESC';
    const page = 0;
    const size = 5;
    const url = `http://localhost:8080/api/v1/products/denormalized/no-brand?page=${page}&size=${size}&productSort=${specificSort}`;

    const res = http.get(url, { tags: { productSort: specificSort } });

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response body has 5 items': (r) => hasValidBody(r),
    });

    sleep(0.1);
}
