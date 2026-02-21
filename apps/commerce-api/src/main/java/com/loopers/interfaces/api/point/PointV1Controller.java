package com.loopers.interfaces.api.point;

import com.loopers.domain.point.PointInfoResult;
import com.loopers.domain.point.PointService;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/points")
public class PointV1Controller implements PointV1ApiSpec {

  private final PointService pointService;

  @Override
  @GetMapping
  public ApiResponse<PointV1Dto.Response.Point> getPoint(@RequestHeader(ApiHeaders.USER_ID) String userId) {
    PointInfoResult pointInfoResult = pointService.getPoint(userId);

    return ApiResponse.success(PointV1Dto.Response.Point.of(pointInfoResult.userId(), pointInfoResult.value()));
  }

  @Override
  @PostMapping("/charge")
  public ApiResponse<PointV1Dto.Response.Point> chargePoint(@RequestHeader(ApiHeaders.USER_ID) String userId,
      @RequestBody PointV1Dto.Request.Charge request) {
    PointInfoResult pointInfoResult = pointService.charge(userId, request.value());

    return ApiResponse.success(PointV1Dto.Response.Point.of(pointInfoResult.userId(), pointInfoResult.value()));
  }
}
