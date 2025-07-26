package com.loopers.interfaces.api.user;

import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "User V1 API", description = "User 관련 API 입니다.")
public interface UserV1ApiSpec {

    @Operation(
            summary = "회원가입",
            description = "회원가입"
    )
    ApiResponse<UserV1Dto.UserResponse> register(
            @Schema(name = "회원가입 요청", description = "회원가입에 필요한 정보")
            @RequestBody UserV1Dto.UserRequest userRequest
    );

    @Operation(
            summary = "유저 정보 조회",
            description = "ID 로 유저 정보를 조회합니다."
    )
    ApiResponse<UserV1Dto.UserResponse> getUser(
            @Schema(name = "유저 ID", description = "조회할 유저의 ID")
            @RequestHeader(ApiHeaders.USER_ID) String userId
    );

}
