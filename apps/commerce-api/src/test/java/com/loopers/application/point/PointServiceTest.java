package com.loopers.application.point;

import com.loopers.domain.point.PointService;
import com.loopers.domain.point.exception.PointException;
import com.loopers.domain.point.PointInfoResult;
import com.loopers.domain.user.exception.UserException;
import com.loopers.domain.point.Point;
import com.loopers.infrastructure.point.PointRepositoryImpl;
import com.loopers.infrastructure.point.PointEntity;
import com.loopers.infrastructure.point.PointJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @InjectMocks
    PointService pointService;

    @Mock
    PointJpaRepository pointJpaRepository;

    PointRepositoryImpl pointRepositoryImpl;

    @BeforeEach
    void setUp() {
        pointRepositoryImpl = spy(new PointRepositoryImpl(pointJpaRepository));
        pointService = new PointService(pointRepositoryImpl);
    }

    @Nested
    @DisplayName("포인트 조회")
    class getPoint {

        @DisplayName("해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.")
        @Test
        void getPointWhenUserExists() {
            // given
            String userId = "geonhee";
            Long expectedPoint = 100L;

            PointEntity pointEntity = PointEntity.from(Point.create(userId, expectedPoint));

            doReturn(Optional.of(pointEntity)).when(pointJpaRepository).findByUserId(userId);

            // when;
            PointInfoResult pointInfoResult = pointService.getPoint(userId);

            // then
            assertAll(
                    () -> assertThat(pointInfoResult.userId()).isEqualTo(userId),
                    () -> assertThat(pointInfoResult.value()).isEqualTo(expectedPoint)
            );

        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, 예외가 발생한다.")
        @Test
        void getNullWhenUserDoesNotExist() {
            // given
            String userId = "geonhee";

            doReturn(Optional.empty()).when(pointJpaRepository).findByUserId(userId);

            // when , then
            assertThatThrownBy(() -> {
                pointService.getPoint(userId);
            }).isInstanceOf(UserException.UserNotFoundException.class);
        }

    }


    @Nested
    @DisplayName("POST /api/v1/points/charge")
    class chargePoint {

        @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.")
        @Test
        void chargeFail() {
            // given
            String userId = "nonExistentUser";

            // when , then
            assertThatThrownBy(() -> {
                pointService.charge(userId, 10000L);
            }).isInstanceOf(UserException.UserNotFoundException.class);
        }

        @DisplayName("충전 요청에 성공할 경우, 충전된 보유 총 포인트가 반환된다.")
        @Test
        void chargeSuccess() {
            // given
            String userId = "geonhee";

            PointEntity pointEntity = PointEntity.from(Point.create(userId, 50000L));

            doReturn(Optional.of(pointEntity)).when(pointJpaRepository).findByUserId(userId);

            // when
            PointInfoResult pointInfoResult = pointService.charge(userId, 10000L);

            // then
            assertAll(
                    () -> assertThat(pointInfoResult.userId()).isEqualTo(userId),
                    () -> assertThat(pointInfoResult.value()).isEqualTo(60000L)
            );
        }

        @DisplayName("충전할 포인트가 0이거나 음수면 PointInvalidChargeAmountException 예외가 발생한다.")
        @ParameterizedTest
        @ValueSource(longs = {
                -0L,
                -1,
                -1000L,
        })
        void chargeFail_whenChargeValueIsZeroOrLess(Long value) {
            // given
            String userId = "geonhee";

            // when
            doReturn(Optional.of(Point.create(userId, 0L))).when(pointRepositoryImpl).findByUserId(userId);

            // then
            assertThatThrownBy(() -> {
                pointService.charge(userId, value);
            }).isInstanceOf(PointException.PointInvalidChargeAmountException.class);

        }
    }

}
