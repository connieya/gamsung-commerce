package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeFacade;
import com.loopers.application.like.LikeResult;
import com.loopers.domain.like.LikeInfo;
import com.loopers.domain.like.LikeTargetType;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/likes")
public class LikeV1Controller implements LikeV1ApiSpec {

    private final LikeFacade likeFacade;

    @PostMapping("/{targetType}/{targetId}")
    @Override
    public ApiResponse<LikeV1Dto.Response.LikeAction> add(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @PathVariable("targetType") LikeTargetType targetType,
            @PathVariable("targetId") Long targetId) {
        LikeInfo likeInfo = likeFacade.add(userId, targetId, targetType);
        return ApiResponse.success(LikeV1Dto.Response.LikeAction.from(likeInfo));
    }

    @DeleteMapping("/{targetType}/{targetId}")
    @Override
    public ApiResponse<LikeV1Dto.Response.LikeAction> remove(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @PathVariable("targetType") LikeTargetType targetType,
            @PathVariable("targetId") Long targetId) {
        LikeInfo likeInfo = likeFacade.remove(userId, targetId, targetType);
        return ApiResponse.success(LikeV1Dto.Response.LikeAction.from(likeInfo));
    }

    @GetMapping("/{targetType}")
    @Override
    public ApiResponse<LikeV1Dto.Response.LikedProducts> getMyLikes(
            @RequestHeader(ApiHeaders.USER_ID) String userId,
            @PathVariable("targetType") LikeTargetType targetType) {
        LikeResult.LikedProducts result = likeFacade.getLikedProducts(userId, targetType);
        return ApiResponse.success(LikeV1Dto.Response.LikedProducts.from(result));
    }
}
