package com.loopers.interfaces.api.internal;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ApiResponse<InternalDto.UserResponse> getUser(@PathVariable("userId") String userId) {
        User user = userService.findByUserId(userId);
        return ApiResponse.success(new InternalDto.UserResponse(user.getId(), user.getUserId(), user.getEmail()));
    }
}
