package com.loopers.domain.user;

import com.loopers.domain.user.vo.Gender;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class UserRegisterCommand {

    private String userId;
    private String email;
    private String birthDate;
    private Gender gender;

    public static UserRegisterCommand of(String userId, String email, String birthDate ,Gender gender) {
        UserRegisterCommand command = new UserRegisterCommand();
        command.userId = userId;
        command.email = email;
        command.birthDate = birthDate;
        command.gender = gender;

        return command;
    }

}
