# 단위 테스트 예시 패턴

## Service 단위 테스트

```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void findById_존재하는상품_상품정보반환한다() {
        // given
        Long productId = 1L;
        Product product = Product.builder()
            .name("테스트 상품")
            .price(10000)
            .build();
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when
        ProductInfo result = productService.findById(productId);

        // then
        assertThat(result.name()).isEqualTo("테스트 상품");
        assertThat(result.price()).isEqualTo(10000);
    }

    @Test
    void findById_존재하지않는상품_예외발생한다() {
        // given
        Long productId = 999L;
        given(productRepository.findById(productId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.findById(productId))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("상품을 찾을 수 없습니다");
    }

    @Test
    void create_유효한커맨드_상품저장하고반환한다() {
        // given
        ProductCommand.Create command = new ProductCommand.Create("신상품", 20000);
        Product savedProduct = Product.builder()
            .name("신상품")
            .price(20000)
            .build();
        given(productRepository.save(any(Product.class))).willReturn(savedProduct);

        // when
        ProductInfo result = productService.create(command);

        // then
        assertThat(result.name()).isEqualTo("신상품");
        verify(productRepository).save(any(Product.class));
    }
}
```

## Domain 엔티티 단위 테스트

```java
class ProductTest {

    @Test
    void of_유효한입력_상품생성한다() {
        // given & when
        Product product = Product.of("상품명", 5000);

        // then
        assertThat(product.getName()).isEqualTo("상품명");
        assertThat(product.getPrice()).isEqualTo(5000);
    }

    @Test
    void of_음수가격_예외발생한다() {
        // given & when & then
        assertThatThrownBy(() -> Product.of("상품명", -1))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```
