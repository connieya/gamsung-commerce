package com.loopers.domain.user;

import com.loopers.domain.user.vo.Gender;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserInfoResult {
    private String userId;
    private String email;
    private LocalDate birthdate;
    private Gender gender;

    public static UserInfoResult of(String userId, String email, LocalDate birthdate, Gender gender) {
        UserInfoResult userInfoResult = new UserInfoResult();

        userInfoResult.userId = userId;
        userInfoResult.email = email;
        userInfoResult.birthdate = birthdate;
        userInfoResult.gender = gender;

        return userInfoResult;

    }


}
