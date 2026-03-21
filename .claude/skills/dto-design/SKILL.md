---
name: dto-design
description: >
  interfaces/api 레이어 DTO 코드(XxxV1Dto.java) 작성·수정 시 반드시 적용한다.
  새 도메인 구현, 기능 추가로 Controller·DTO 파일을 만들 때도 적용한다.
  Request/Response 중첩 구조, record 사용, from() 팩토리 메서드, 레이어 의존성 규칙 포함.
---

# DTO 설계 규칙 (interfaces 레이어)

## 파일 단위

- API 도메인별 하나의 파일: `XxxV1Dto.java`
- 해당 API의 모든 Request/Response를 inner class로 묶는다

## 구조

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

## 규칙

- **Request/Response 그룹핑**: 반드시 `Request`, `Response` static class로 분리
- **record 사용**: DTO는 record로 정의 (불변, 간결)
- **중첩 깊이**: 최대 3단까지 허용 (`XxxV1Dto > Request/Response > 구체 DTO > 하위 구조체`)
- **from() 팩토리 메서드**: Response record에 `static from(XxxInfo)` 메서드로 Info → Response 변환
- **컨트롤러 변환 원칙**: Request → Criteria/Command 변환은 Controller에서 수행 (Facade/Service에서 하지 않음)

## 예시: from() 팩토리 메서드

```java
public class ProductV1Dto {
    public static class Response {
        public record Detail(
            Long id,
            String name,
            int price
        ) {
            public static Detail from(ProductInfo info) {
                return new Detail(info.id(), info.name(), info.price());
            }
        }
    }
}
```

## 레이어 의존성 주의

- `XxxV1Dto`는 `interfaces/api` 레이어에만 존재
- `application`(Facade/Criteria) 또는 `domain`에서 `XxxV1Dto`를 import하면 **레이어 위반**
- Facade가 필요한 데이터는 `XxxInfo` record(domain 레이어)를 통해 전달
