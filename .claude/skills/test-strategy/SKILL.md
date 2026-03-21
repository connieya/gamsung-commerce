---
name: test-strategy
description: >
  테스트 파일(XxxTest.java) 작성 시 반드시 적용한다.
  새 도메인 구현, 기능 추가로 테스트 코드를 생성할 때도 적용한다.
  JUnit5 + Mockito 단위 테스트, @DataJpaTest, @WebMvcTest, @SpringBootTest 패턴 포함.
---

# 테스트 코드 작성 전략

## 파일 위치

- 테스트 파일은 `src/test/java/com/loopers/` 하위에 작성
- 테스트 대상 클래스와 동일한 패키지 구조 유지
  - `domain/product/ProductService.java` → `domain/product/ProductServiceTest.java`

## 테스트 메서드 네이밍

```
메서드명_상황_기대결과()
```

예시:
- `findById_존재하는상품_반환한다()`
- `findById_존재하지않는상품_예외발생한다()`
- `create_정상입력_상품저장후반환한다()`

## given / when / then 패턴

모든 테스트는 given/when/then 구조로 작성한다.

```java
@Test
void findById_존재하는상품_반환한다() {
    // given
    Long productId = 1L;
    Product product = Product.of("상품명", 10000);
    given(productRepository.findById(productId)).willReturn(Optional.of(product));

    // when
    ProductInfo result = productService.findById(productId);

    // then
    assertThat(result.name()).isEqualTo("상품명");
}
```

## 단위 테스트 (Service, Domain 로직)

- `@ExtendWith(MockitoExtension.class)` 사용
- `@Mock`: 의존 객체 목킹
- `@InjectMocks`: 테스트 대상 객체
- `given(...).willReturn(...)` / `verify(...)` 패턴 사용

예시 파일: `examples/unit-test-example.md`

## 슬라이스 테스트

| 레이어 | 애너테이션 | 용도 |
|--------|-----------|------|
| JPA Repository | `@DataJpaTest` | 쿼리 검증, 영속성 테스트 |
| Controller | `@WebMvcTest` | 요청/응답 포맷, 유효성 검증 |

- `@DataJpaTest`: 인메모리 DB(H2) 사용, 트랜잭션 자동 롤백
- `@WebMvcTest`: MockMvc로 HTTP 계층만 테스트

## 통합 테스트

- `@SpringBootTest`: 전체 컨텍스트 로드, 실제 DB 사용
- 테스트 격리: `@Transactional` + `@Rollback` 또는 `@BeforeEach`에서 데이터 정리

예시 파일: `examples/spring-boot-test-example.md`

## AssertJ 사용

- `assertThat(actual).isEqualTo(expected)`
- `assertThat(actual).isNotNull()`
- `assertThatThrownBy(() -> ...).isInstanceOf(XxxException.class)`
- `assertThat(list).hasSize(n).containsExactly(...)`
