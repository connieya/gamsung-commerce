package com.loopers.interfaces.api.point;

import com.loopers.application.point.port.in.PointInfoResult;
import com.loopers.application.point.port.in.PointUseCase;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/points")
public class PointV1Controller implements PointV1ApiSpec {

    private final PointUseCase pointUseCase;

    @Override
    @GetMapping
    public ApiResponse<PointV1Dto.PointResponse> getPoint(@RequestHeader("X-USER-ID") String userId) {
        PointInfoResult pointInfoResult = pointUseCase.getPoint(userId);

        return ApiResponse.success(PointV1Dto.PointResponse.of(pointInfoResult.userId(), pointInfoResult.value()));
    }


    @Override
    @PostMapping("/charge")
    public ApiResponse<PointV1Dto.PointResponse> chargePoint(String userId, PointV1Dto.PointRequest request) {
        PointInfoResult pointInfoResult = pointUseCase.charge(userId, request.value());

        return ApiResponse.success(PointV1Dto.PointResponse.of(pointInfoResult.userId(), pointInfoResult.value()));
    }
}
