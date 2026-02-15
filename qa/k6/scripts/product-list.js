import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * 시나리오: 상품 목록 API (일반) — 비정규화 전, product_like JOIN+COUNT 경로
 * GET /api/v1/products?page=0&size=5&productSort={SORT_TYPE}
 * 기본 정렬 LIKES_DESC: 좋아요 집계 부하가 커서 비정규화 전/후 비교에 적합
 */
export const options = {
    vus: 10,      // 동시 가상 사용자 (CLI 오버라이드: -u 50)
    duration: '60s', // 테스트 시간 (CLI 오버라이드: -d 120s)
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
    const specificSort = __ENV.SORT_TYPE || 'LIKES_DESC';
    const page = 0;
    const size = 5;
    const url = `http://localhost:8080/api/v1/products?page=${page}&size=${size}&productSort=${specificSort}`;

    const res = http.get(url, { tags: { productSort: specificSort } });

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response body has 5 items': (r) => hasValidBody(r),
    });

    sleep(0.1);
}
