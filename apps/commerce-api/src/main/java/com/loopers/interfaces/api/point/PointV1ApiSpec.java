package com.loopers.interfaces.api.point;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Point V1 API", description = "포인트 관련 API 입니다.")
public interface PointV1ApiSpec {

    @Operation(
            summary = "포인트 조회",
            description = "유저 ID로 포인트를 조회합니다."
    )
    ApiResponse<PointV1Dto.PointResponse> getPoint(
            @Schema(name = "유저 ID", description = "포인트를 조회할 유저의 ID", example = "user123")
            @RequestHeader("X-USER-ID") String userId
    );

    @Operation(
            summary = "포인트 충전",
            description = "유저의 포인트를 충전합니다."
    )
    ApiResponse<PointV1Dto.PointResponse> chargePoint(
        @Schema(name = "유저 ID , 충전할 포인트 " , description = "충전할 포인트")
        @RequestHeader("X-USER-ID") String userId, @RequestBody PointV1Dto.PointRequest request
    );
}
