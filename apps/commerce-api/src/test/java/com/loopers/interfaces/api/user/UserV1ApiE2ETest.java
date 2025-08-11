package com.loopers.interfaces.api.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.vo.Gender;
import com.loopers.infrastructure.user.UserEntity;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserV1ApiE2ETest {

    private static final String ENDPOINT_POST = "/api/v1/users";
    private static final String ENDPOINT_GET = "/api/v1/users/me";

    private final TestRestTemplate testRestTemplate;
    private final UserJpaRepository userJpaRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public UserV1ApiE2ETest(
            TestRestTemplate testRestTemplate,
            UserJpaRepository userJpaRepository,
            DatabaseCleanUp databaseCleanUp
    ) {
        this.testRestTemplate = testRestTemplate;
        this.userJpaRepository = userJpaRepository;
        this.databaseCleanUp = databaseCleanUp;
    }


    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("POST /api/v1/users")
    @Nested
    class RegisterUser {
        @DisplayName("회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다")
        @Test
        void returnsCreatedUser_whenRegistrationIsSuccessful() {
            // given
            HttpEntity<UserV1Dto.UserRequest> httpEntity = new HttpEntity<>(new UserV1Dto.UserRequest(
                    "geonhee",
                    "geonhee@naver.com",
                    "1994-09-26",
                    Gender.MALE
            ));
            // when
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(ENDPOINT_POST, HttpMethod.POST, httpEntity, responseType);

            //then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().id()).isEqualTo("geonhee"),
                    () -> assertThat(response.getBody().data().email()).isEqualTo("geonhee@naver.com"),
                    () -> assertThat(response.getBody().data().birthDate()).isEqualTo("1994-09-26")
            );

        }

        @DisplayName("회원 가입 시에 성별이 없을 경우, 400 Bad Request 응답을 반환한다.")
        @Test
        void thorwsBadRequest_whenGenderIsNotProvided() {
            // given
            HttpEntity<UserV1Dto.UserRequest> httpEntity = new HttpEntity<>(new UserV1Dto.UserRequest(
                    "geonhee",
                    "geonhee@naver.com",
                    "1994-09-26",
                    null));

            // when
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response = testRestTemplate.exchange(ENDPOINT_POST, HttpMethod.POST, httpEntity, responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is4xxClientError()),
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            );

        }
    }

    @DisplayName("GET /api/v1/users/me")
    @Nested
    class GetUser {
        @DisplayName("내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsUserInfo_whenUserIdIsProvided() {
            // given
            userJpaRepository.save(UserEntity.fromDomain(User.create("geonhee", "geonhee@naver.com", "1994-09-26", Gender.MALE)));
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {
            };

            String userId = "geonhee";
            HttpHeaders headers = new HttpHeaders();
            headers.add(ApiHeaders.USER_ID, userId);


            // when
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response = testRestTemplate.exchange(ENDPOINT_GET, HttpMethod.GET, new HttpEntity<>(null, headers), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().id()).isEqualTo("geonhee"),
                    () -> assertThat(response.getBody().data().email()).isEqualTo("geonhee@naver.com"),
                    () -> assertThat(response.getBody().data().birthDate()).isEqualTo("1994-09-26")
            );
        }

        @DisplayName("존재하지 않는 ID 로 조회할 경우, 404 Not Found 응답을 반환한다.")
        @Test
        void returnsNotFound_whenUserDoesNotExist() {
            // given
            userJpaRepository.save(UserEntity.fromDomain(User.create("geonhee", "geonhee@naver.com", "1994-09-26", Gender.MALE)));
            String userId = "nonexistent";
            HttpHeaders headers = new HttpHeaders();
            headers.add(ApiHeaders.USER_ID, userId);

            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {
            };

            // when
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response = testRestTemplate.exchange(ENDPOINT_GET, HttpMethod.GET, new HttpEntity<>(null, headers), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is4xxClientError()),
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND)
            );
        }


    }
}
