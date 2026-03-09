package com.loopers.interfaces.api.like;

import com.loopers.domain.like.LikeTargetType;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Like V1 API", description = "좋아요 관련 API")
public interface LikeV1ApiSpec {

    @Operation(summary = "좋아요 등록", description = "대상에 좋아요를 등록합니다.")
    ApiResponse<LikeV1Dto.Response.LikeAction> add(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @PathVariable("targetType") LikeTargetType targetType,
            @PathVariable("targetId") Long targetId
    );

    @Operation(summary = "좋아요 취소", description = "대상에 등록된 좋아요를 취소합니다.")
    ApiResponse<LikeV1Dto.Response.LikeAction> remove(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @PathVariable("targetType") LikeTargetType targetType,
            @PathVariable("targetId") Long targetId
    );

    @Operation(summary = "내 좋아요 목록 조회", description = "타입별 내가 좋아요한 목록을 조회합니다.")
    ApiResponse<LikeV1Dto.Response.LikedProducts> getMyLikes(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @PathVariable("targetType") LikeTargetType targetType
    );
}
