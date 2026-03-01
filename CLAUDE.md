# CLAUDE.md - gamsung-commerce (Backend API)

## 프로젝트 개요
무신사 스타일 이커머스 백엔드 API. Spring Boot + Gradle 멀티모듈 구조.

## 기술 스택
- Java 17+, Spring Boot, Gradle
- MySQL, Redis (캐시), Kafka
- JPA/Hibernate, Spring Data

## 프로젝트 구조
```
apps/commerce-api/src/main/java/com/loopers/
├── domain/          # 도메인 레이어 (Entity, Service, Repository 인터페이스, Info, Command)
├── infrastructure/  # 인프라 레이어 (JPA Repository 구현체, Cache 구현체)
├── interfaces/api/  # API 레이어 (Controller, Dto, ApiSpec)
└── support/         # 공통 지원 (ErrorType, BaseEntity 등)
```

## 아키텍처 패턴
- **레이어드 아키텍처**: interfaces → application → domain ← infrastructure
- **도메인 서비스**: `XxxService`는 domain 패키지에 위치
- **Repository 패턴**: domain에 인터페이스, infrastructure에 구현체
- **캐시 패턴**: Redis 캐시 우선 조회 후 DB fallback (`BrandCacheRepository` 참고)
- **API 응답 래퍼**: `ApiResponse<T>` (meta + data 구조)
- **API 스펙 인터페이스**: Swagger 문서화용 `XxxV1ApiSpec` 인터페이스를 Controller가 구현
- **DTO 패턴**: 아래 "DTO 설계 규칙" 참고
- **도메인 정보 객체**: `XxxInfo` record로 도메인 데이터 전달
- **커맨드 객체**: `XxxCommand` 내부에 요청 데이터 정의

## DTO 설계 규칙 (interfaces 레이어)

### 파일 단위
- API 도메인별 하나의 파일: `XxxV1Dto.java`
- 해당 API의 모든 Request/Response를 inner class로 묶는다

### 구조
```java
public class OrderV1Dto {
    public static class Request {
        public record Place(...) {}       // 2단 중첩: XxxV1Dto.Request.Place
        public record Ready(...) {}
    }
    public static class Response {
        public record Detail(...) {
            public record LineItem(...) {} // 3단 중첩: 해당 Response 전용 하위 구조체
        }
    }
}
```

### 규칙
- **Request/Response 그룹핑**: 반드시 `Request`, `Response` static class로 분리
- **record 사용**: DTO는 record로 정의 (불변, 간결)
- **중첩 깊이**: 최대 3단까지 허용 (`XxxV1Dto > Request/Response > 구체 DTO > 하위 구조체`)
- **from() 팩토리 메서드**: Response record에 `static from(Result/Info)` 메서드로 변환
- **컨트롤러 변환 원칙**: Request → Criteria/Command 변환은 Controller에서 수행

## 레이어 의존성 규칙 (필수)
각 레이어는 아래 방향으로만 의존할 수 있다. **역방향 import는 절대 금지.**

```
interfaces/api  →  application  →  domain  ←  infrastructure
```

| 레이어 | import 가능 | import 금지 |
|--------|------------|-------------|
| `domain` | `domain`, `support` | `interfaces`, `application`, `infrastructure` |
| `application` | `application`, `domain`, `support` | `interfaces` |
| `infrastructure` | `infrastructure`, `domain`, `support` | `interfaces`, `application` |
| `interfaces/api` | 모든 레이어 | — |

### 실수하기 쉬운 케이스
- `application` 패키지의 Criteria/Facade에서 `interfaces.api.XxxV1Dto`를 import하면 **위반**
- 대신 `application` 레이어 전용 타입(예: `PaymentCriteria.OrderItem`)을 정의하고, Controller에서 Dto → Criteria 변환
- `domain`에서 `application`의 Facade나 Criteria를 import하면 **위반**
- `application`(Facade)에서 `Repository`를 직접 호출하면 **위반** — 반드시 `XxxService`를 통해 호출 (Facade → Service → Repository)

## 연관 프로젝트
- 프론트엔드: `/Users/cony/Desktop/workspace/gamsung-web` (Next.js)

## 개발 워크플로우 (필수)

### 브랜치 전략
- **main 직접 push 금지** — 모든 변경은 PR을 통해서만 merge
- 브랜치 네이밍: `{type}/GS-{issue번호}-{간단설명}`
  - 예: `feature/GS-42-cart-domain`, `fix/GS-55-point-restore`, `refactor/GS-60-cache-ttl`
- PR merge 방식: **Squash Merge** (히스토리 깔끔하게 유지)

### Issue 기반 개발
- 모든 작업은 **GitHub Issue 등록**으로 시작한다
- Issue 템플릿: feature(기능), bug(버그), task(리팩토링/인프라)
- PR 생성 시 본문에 `Closes #이슈번호`로 자동 연결

### 작업 흐름
```
1. Issue 등록 (요구사항 + 인수 조건)
2. feature 브랜치 생성
3. 도메인 스펙 문서 작성 (해당 시)
4. 구현 + 테스트
5. PR 생성 → 리뷰
6. Squash Merge → main
```

## Git 커밋 메시지 규칙

### 형식
`<type>(<scope>): <subject>`

### 언어
- **subject와 본문은 반드시 한글**로 작성. type과 scope만 영문 소문자.
- subject: 한 줄, 50자 내외, 명령형, 마침표 없음

### Type
feat(기능), fix(버그수정), docs(문서), style(포맷), refactor(리팩터링), test(테스트), chore(빌드/설정)

### Scope
변경된 모듈/도메인: brand, product, order, payment, likes 등

### 예시
```
feat(product): 상품 목록 좋아요 정렬 추가
fix(payment): PG 타임아웃 시 재시도 로직 수정
refactor(likes): LikeProductFacade로 좋아요 조회 통합
```

한 커밋에 여러 도메인이 있으면 가장 비중 큰 변경 기준으로 scope 하나만 사용.

## 디버깅 원칙 (필수)

에러(특히 500, 런타임 실패) 진단 시 **추측부터 하지 말고 사실부터 확인**한다.

### 순서
1. **실제 에러 메시지·스택 트레이스 확인** — 로그 또는 응답 body에서 원문을 먼저 읽는다
2. **DB 스키마·데이터 확인** — 컬럼 누락, 타입 불일치, NULL 여부 등 데이터 레벨 원인을 먼저 배제한다
3. **코드 레벨 원인 분석** — 위 두 단계에서 원인이 안 나올 때만 JPQL, 로직 오류 등을 의심한다

### 하지 말 것
- 에러 메시지를 읽기 전에 "아마 이 코드가 문제일 것" 식으로 추정하지 않는다
- 명백한 코드 경로가 원인이라고 가정하지 않는다 — 반드시 로그/스택 트레이스로 검증한다
- 한 가지 가설에 고착하지 않는다 — 첫 시도가 실패하면 다른 레이어(DB, 설정, 데이터)를 점검한다

## README.md 자동 동기화 규칙 (필수)

아키텍처에 영향을 주는 변경을 수행한 뒤에는 **반드시 `README.md`를 함께 갱신**한다.

### 트리거 조건 (하나라도 해당되면 갱신)
- 파일/폴더 **생성·삭제·이동** → 프로젝트 구조 트리 갱신
- 새 도메인 모듈 추가 또는 기존 모듈 삭제 → 모듈 목록 갱신
- 새 레이어·패키지 추가 또는 삭제 → 구조 트리 갱신
- 아키텍처 패턴 변경 (새 패턴, 레이어 규칙 등) → 해당 섹션 갱신
- 기술 스택 추가·제거 → 기술 스택 갱신

### 갱신 대상 섹션
| 변경 유형 | README 섹션 |
|-----------|------------|
| 폴더/파일 구조 | `프로젝트 구조` 트리 |
| 도메인 모듈 | `프로젝트 구조` + 모듈 설명 |
| API 엔드포인트 | `주요 API` 테이블 |
| 기술 스택 | `기술 스택` |
| 환경 변수 | `환경 변수` 테이블 |

### 주의
- 커밋 전에 README.md 변경 여부를 자체 점검할 것
- 내용은 사실(코드)에 기반하여 작성하고, 추측이나 미래 계획은 적지 않을 것
- README.md에 불필요한 상세 구현은 넣지 않고, 구조와 사용법 수준만 유지할 것

## LLM 행동 가이드라인

- **한국어 응답**: 코드와 기술 용어를 제외한 모든 응답은 한국어로 작성
- **환각 금지**: 존재하지 않는 API, 패키지, 파일 경로를 지어내지 마라. 확실하지 않으면 먼저 확인
- **점진적 실행**: 여러 파일을 동시에 변경하지 말고, 한 단위씩 변경 후 중간 검증
- **피드백 기록**: 사용자가 실수를 지적하면 MEMORY.md에 교훈 기록
