package com.loopers.interfaces.api.user;

import com.loopers.domain.user.UserInfoResult;
import com.loopers.domain.user.UserRegisterResult;
import com.loopers.domain.user.vo.Gender;

import java.time.LocalDate;

public class UserV1Dto {
    public record UserResponse(
            String id,
            LocalDate birthDate,
            String email
    ) {
        public static UserResponse from(UserRegisterResult userRegisterResult) {
            return new UserResponse(
                    userRegisterResult.id(),
                    userRegisterResult.birthDate(),
                    userRegisterResult.email()
            );
        }

        public static UserResponse from(UserInfoResult userInfoResult) {
            return new UserResponse(
                    userInfoResult.getUserId(),
                    userInfoResult.getBirthdate(),
                    userInfoResult.getEmail()
            );
        }
    }

    public record UserRequest(
            String userId,
            String email,
            String birthDate,
            Gender gender
    ) {

    }


}
