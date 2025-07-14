package com.loopers.domain.user;

import com.loopers.domain.common.SelfValidating;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class User extends SelfValidating<User> {

    @Pattern(regexp = "^[a-zA-Z0-9]{1,10}$", message = "ID는 영문 및 숫자 10자 이내여야 합니다.")
    @NotBlank
    private String id;

    @Email
    @NotBlank
    private String email;

    private BirthDate birthDay;

    public static User create(String id, String email, String birthDay) {
        User user = new User();

        user.id = id;
        user.email = email;
        user.birthDay = new BirthDate(birthDay);

        user.validateSelf();

        return user;
    }
}
