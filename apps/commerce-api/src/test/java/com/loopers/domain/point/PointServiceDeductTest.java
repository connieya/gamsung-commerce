package com.loopers.domain.point;

import com.loopers.domain.point.exception.PointException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceDeductTest {

    @InjectMocks
    PointService pointService;

    @Mock
    PointRepository pointRepository;

    @Test
    @DisplayName("주문한 총 금액만큼 포인트를 차감한다.")
    void deduct() {
        // given
        when(pointRepository.findPointForUpdate("gunny")).thenReturn(Optional.of(Point.create("gunny", 20000L)));

        // when
        pointService.deduct("gunny", 10000L);

        // then
        verify(pointRepository, times(1)).save(any(Point.class));
    }

    @Test
    @DisplayName("주문한 총 금액 보다 보유 포인트가 작으면 PointInsufficientException 예외가 발생한다.")
    void deduct_Fail() {
        // given
        when(pointRepository.findPointForUpdate("gunny")).thenReturn(Optional.of(Point.create("gunny", 5000L)));

        // when & then
        assertThatThrownBy(() -> {
            pointService.deduct("gunny", 10000L);
        }).isInstanceOf(PointException.PointInsufficientException.class);


    }

}
