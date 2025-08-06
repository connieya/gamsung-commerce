package com.loopers.interfaces.api.likes;

import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Procuct Like V1 API", description = "좋아요 관련 API 입니다.")
public interface ProductLikeV1ApiSpec {

    @Operation(
            summary = "좋아요 등록",
            description = "상품에 좋아요를 등록합니다."
    )
    ApiResponse<?> add(
            @RequestHeader(ApiHeaders.USER_ID) String userId ,@PathVariable("productId") Long productId
    );

    @Operation(
            summary = "좋아요 취소",
            description = "상품에 등록된 좋아요를 취소합니다."
    )
    ApiResponse<?> remove(
            @RequestHeader(ApiHeaders.USER_ID) String userId , @PathVariable("productId") Long productId
    );

    @Operation(
            summary = "내가 좋아요 한 상품 목록 조회",
            description = "유저 ID 로 좋아요 한 상품 목록을 조회합니다."
    )
    ApiResponse<?> getMyLikes(
            @RequestHeader(ApiHeaders.USER_ID) String userId
    );
}
