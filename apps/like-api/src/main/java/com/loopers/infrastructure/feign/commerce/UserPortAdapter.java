package com.loopers.infrastructure.feign.commerce;

import com.loopers.domain.like.port.UserPort;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPortAdapter implements UserPort {

    private final CommerceApiClient commerceApiClient;

    @Override
    public UserInfo getUser(String userId) {
        CommerceApiDto.UserResponse user = commerceApiClient.getUser(userId).data();
        if (user == null) {
            throw new CoreException(ErrorType.USER_NOT_FOUND);
        }
        return new UserInfo(user.id(), user.userId(), user.email());
    }
}
