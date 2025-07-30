package com.loopers.interfaces.api.point;

import com.loopers.domain.point.Point;
import com.loopers.infrastructure.point.entity.PointEntity;
import com.loopers.infrastructure.point.jpa.PointJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PointV1ApiE2ETest {

    private static final String ENDPOINT_GET = "/api/v1/points";
    private static final String ENDPOINT_POST = "/api/v1/points/charge";

    private final TestRestTemplate testRestTemplate;
    private final DatabaseCleanUp databaseCleanUp;
    private final PointJpaRepository pointJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Autowired
    public PointV1ApiE2ETest(TestRestTemplate testRestTemplate, DatabaseCleanUp databaseCleanUp, PointJpaRepository pointJpaRepository, UserJpaRepository userJpaRepository) {
        this.testRestTemplate = testRestTemplate;
        this.databaseCleanUp = databaseCleanUp;
        this.pointJpaRepository = pointJpaRepository;
        this.userJpaRepository = userJpaRepository;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("GET /api/v1/points")
    @Nested
    class GetPoint {
        @DisplayName("포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.")
        @Test
        void getPoint() {
            // given
            pointJpaRepository.save(PointEntity.from(Point.create("geonhee", 10L)));

            String userId = "geonhee";
            HttpHeaders headers = new HttpHeaders();
            headers.add(ApiHeaders.USER_ID, userId);

            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {
            };

            // when
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response = testRestTemplate.exchange(ENDPOINT_GET, HttpMethod.GET, new HttpEntity<>(null, headers), responseType);


            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().userId()).isEqualTo(userId),
                    () -> assertThat(response.getBody().data().value()).isEqualTo(10L)

            );
        }

        @DisplayName("X-USER-ID 헤더가 없을 경우, 400 Bad Request 응답을 반환한다.")
        @Test
        void getPointWithoutUserId() {
            // given
            HttpHeaders headers = new HttpHeaders();

            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {
            };


            // when
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response = testRestTemplate.exchange(ENDPOINT_GET, HttpMethod.GET, new HttpEntity<>(null, headers), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is4xxClientError()),
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            );
        }
    }

    @Nested
    @DisplayName("POST /api/v1/points/charge")
    class ChargePoint {
        @DisplayName("포인트 충전 요청에 성공할 경우, 충전된 보유 총 포인트를 응답으로 반환한다.")
        @Test
        void chargePoint() {
            // given
            pointJpaRepository.save(PointEntity.from(Point.create("geonhee", 1000L)));

            String userId = "geonhee";
            HttpHeaders headers = new HttpHeaders();
            headers.add(ApiHeaders.USER_ID, userId);


            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {
            };

            Long chargeAmount = 50000L;
            PointV1Dto.PointRequest pointRequest = new PointV1Dto.PointRequest(chargeAmount);

            // when
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response = testRestTemplate.exchange(ENDPOINT_POST, HttpMethod.POST, new HttpEntity<>(pointRequest, headers), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().value()).isEven().isEqualTo(chargeAmount + 1000L)
            );

        }

        @DisplayName("존재하지 않는 유저로 요청할 경우, 404 Not Found 응답을 반환한다.")
        @Test
        void chargeFailWhenUserDoesNotExist() {
            // given
            String userId = "nonexistent";
            HttpHeaders headers = new HttpHeaders();
            headers.add(ApiHeaders.USER_ID, userId);

            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {
            };

            Long chargeAmount = 50000L;
            PointV1Dto.PointRequest pointRequest = new PointV1Dto.PointRequest(chargeAmount);

            // when
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response = testRestTemplate.exchange(ENDPOINT_POST, HttpMethod.POST, new HttpEntity<>(pointRequest, headers), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is4xxClientError()),
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND)
            );
        }
    }

}
