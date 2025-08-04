package com.loopers.domain.point;

import com.loopers.domain.user.exception.UserException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService implements PointUseCase {

    private final PointRepository pointRepository;

    @Transactional(readOnly = true)
    @Override
    public PointInfoResult getPoint(String userId) {
        Point point = pointRepository.findByUserId(userId).orElseThrow(()-> new UserException.UserNotFoundException(ErrorType.USER_NOT_FOUND));

        return PointInfoResult.of(point.getUserId(), point.getValue());
    }

    @Transactional
    @Override
    public PointInfoResult charge(String userId, Long value) {
        Point point = pointRepository.findByUserId(userId).orElseThrow(()-> new UserException.UserNotFoundException(ErrorType.USER_NOT_FOUND));

        point.charge(value);

        return PointInfoResult.of(userId, point.getValue());
    }

    @Transactional
    public void deduct(String userId , Long totalAmount) {
        Point point = pointRepository.findByUserId(userId).orElseThrow(() -> new UserException.UserNotFoundException(ErrorType.USER_NOT_FOUND));
        point.deduct(totalAmount);
        pointRepository.save(point);
    }
}
