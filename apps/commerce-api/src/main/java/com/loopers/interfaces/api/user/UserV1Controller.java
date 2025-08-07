package com.loopers.interfaces.api.user;

import com.loopers.domain.user.UserInfoResult;
import com.loopers.domain.user.UserRegisterCommand;
import com.loopers.domain.user.UserRegisterResult;
import com.loopers.domain.user.UserUseCase;
import com.loopers.interfaces.api.ApiHeaders;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserV1Controller implements UserV1ApiSpec {

    private final UserUseCase userUseCase;

    @PostMapping
    @Override
    public ApiResponse<UserV1Dto.UserResponse> register(@RequestBody UserV1Dto.UserRequest userRequest) {
        UserRegisterResult userRegisterResult = userUseCase.register(UserRegisterCommand.of(userRequest.userId(), userRequest.email(), userRequest.birthDate(), userRequest.gender()));
        return ApiResponse.success(UserV1Dto.UserResponse.from(userRegisterResult));
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<UserV1Dto.UserResponse> getUser(@RequestHeader(value = ApiHeaders.USER_ID, required = true) String userId) {
        UserInfoResult user = userUseCase.getUser(userId);
        return ApiResponse.success(UserV1Dto.UserResponse.from(user));
    }
}
