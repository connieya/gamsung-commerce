package com.loopers.application.point;

import com.loopers.application.point.port.in.PointInfoResult;
import com.loopers.application.point.port.in.PointUseCase;
import com.loopers.application.point.port.out.PointRepositoryOut;
import com.loopers.application.user.exception.UserException;
import com.loopers.domain.point.Point;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService implements PointUseCase {

    private final PointRepositoryOut pointRepositoryOut;

    @Transactional(readOnly = true)
    @Override
    public PointInfoResult getPoint(String userId) {
        Point point = pointRepositoryOut.findByUserId(userId).orElseThrow(()-> new UserException.UserNotFoundException(ErrorType.USER_NOT_FOUND));

        return PointInfoResult.of(point.getUserId(), point.getValue());
    }

    @Transactional
    @Override
    public PointInfoResult charge(String userId, Long value) {
        Point point = pointRepositoryOut.findByUserId(userId).orElseThrow(()-> new UserException.UserNotFoundException(ErrorType.USER_NOT_FOUND));

        point.charge(value);

        return PointInfoResult.of(userId, point.getValue());
    }
}
