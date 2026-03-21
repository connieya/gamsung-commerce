# 스프링 부트 테스트 예시 패턴

## @DataJpaTest — JPA Repository 슬라이스 테스트

```java
@DataJpaTest
class ProductJpaRepositoryTest {

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Test
    void findByNameContaining_검색어포함상품_반환한다() {
        // given
        productJpaRepository.save(Product.of("나이키 운동화", 89000));
        productJpaRepository.save(Product.of("아디다스 운동화", 79000));
        productJpaRepository.save(Product.of("나이키 티셔츠", 49000));

        // when
        List<Product> result = productJpaRepository.findByNameContaining("나이키");

        // then
        assertThat(result).hasSize(2)
            .extracting(Product::getName)
            .containsExactlyInAnyOrder("나이키 운동화", "나이키 티셔츠");
    }

    @Test
    void findAllByOrderByCreatedAtDesc_최신순정렬_반환한다() {
        // given
        Product older = productJpaRepository.save(Product.of("오래된 상품", 10000));
        Product newer = productJpaRepository.save(Product.of("새 상품", 20000));

        // when
        List<Product> result = productJpaRepository.findAllByOrderByCreatedAtDesc();

        // then
        assertThat(result.get(0).getName()).isEqualTo("새 상품");
    }
}
```

## @WebMvcTest — Controller 슬라이스 테스트

```java
@WebMvcTest(ProductV1Controller.class)
class ProductV1ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductFacade productFacade;

    @Test
    void getProduct_존재하는상품_200반환한다() throws Exception {
        // given
        Long productId = 1L;
        ProductInfo info = new ProductInfo(productId, "테스트 상품", 10000);
        given(productFacade.getProduct(productId)).willReturn(info);

        // when & then
        mockMvc.perform(get("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(productId))
            .andExpect(jsonPath("$.data.name").value("테스트 상품"));
    }

    @Test
    void createProduct_유효하지않은입력_400반환한다() throws Exception {
        // given
        String invalidBody = """
            {"name": "", "price": -1}
            """;

        // when & then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidBody))
            .andExpect(status().isBadRequest());
    }
}
```

## @SpringBootTest — 통합 테스트

```java
@SpringBootTest
@Transactional
class ProductIntegrationTest {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void createAndFetch_상품생성후조회_일치한다() {
        // given
        ProductCommand.Create command = new ProductCommand.Create("통합테스트 상품", 30000);

        // when
        ProductInfo created = productFacade.createProduct(command);
        ProductInfo fetched = productFacade.getProduct(created.id());

        // then
        assertThat(fetched.name()).isEqualTo("통합테스트 상품");
        assertThat(fetched.price()).isEqualTo(30000);
    }
}
```
