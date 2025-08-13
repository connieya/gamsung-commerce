import http from 'k6/http';
import { check, sleep } from 'k6';

// 테스트 옵션 설정
// 100명 가상 사용자(VU)가 1분(60초) 동안 테스트를 진행
export const options = {
    vus: 1, // 가상 사용자 1명으로 설정
    duration: '60s', // 1분 동안 테스트
};



// 메인 테스트 시나리오
export default function () {

    const specificSort = 'PRICE_ASC';

    const page = 0;
    const size = 5;

    const url = `http://localhost:8080/api/v1/products?page=${page}&size=${size}&productSort=${specificSort}`;

    // GET 요청 실행
    const res = http.get(url);

    // 응답 상태 코드가 200인지 확인
    check(res, {
        'status is 200': (r) => r.status === 200,
        'response body has 5 items': (r) => r.json().data.items.length === 5,
    });

    // 요청 후 0.1초 대기 (실제 사용자 행동 모방)
    sleep(0.1);
}
