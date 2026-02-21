package com.loopers.interfaces.api.user;

import com.loopers.domain.user.UserInfoResult;
import com.loopers.domain.user.UserRegisterResult;
import com.loopers.domain.user.vo.Gender;

import java.time.LocalDate;

public class UserV1Dto {
    public static class Request {
        public record Register(
                String userId,
                String email,
                String birthDate,
                Gender gender
        ) {

        }
    }

    public static class Response {
        public record User(
                String id,
                LocalDate birthDate,
                String email
        ) {
            public static User from(UserRegisterResult userRegisterResult) {
                return new User(
                        userRegisterResult.id(),
                        userRegisterResult.birthDate(),
                        userRegisterResult.email()
                );
            }

            public static User from(UserInfoResult userInfoResult) {
                return new User(
                        userInfoResult.getUserId(),
                        userInfoResult.getBirthdate(),
                        userInfoResult.getEmail()
                );
            }
        }
    }
}
