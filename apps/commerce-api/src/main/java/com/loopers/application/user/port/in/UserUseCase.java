package com.loopers.application.user.port.in;

public interface UserUseCase {

    UserRegisterResult register(UserRegisterCommand command);

    UserInfoResult getUser(String userId);
}
