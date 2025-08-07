package com.loopers.domain.user;

public interface UserUseCase {

    UserRegisterResult register(UserRegisterCommand command);

    UserInfoResult getUser(String userId);

    User findByUserId(String userId);
}
