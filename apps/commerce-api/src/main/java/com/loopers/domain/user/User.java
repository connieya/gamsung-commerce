package com.loopers.domain.user;

import com.loopers.domain.common.Validatable;
import com.loopers.domain.user.vo.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class User extends Validatable<User> {

    @Pattern(regexp = "^[a-zA-Z0-9]{1,10}$", message = "ID는 영문 및 숫자 10자 이내여야 합니다.")
    @NotBlank
    private String id;

    @Pattern(regexp =  "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$" , message = "이메일은 xx@yy.zz 형식이어야 합니다.")
    @NotBlank
    private String email;

    @NotNull
    private BirthDate birthDate;

    @NotNull(message = "성별은 필수입니다.")
    private Gender gender;

    @Builder
    private User(String id, String email, BirthDate birthDate, Gender gender) {
        this.id = id;
        this.email = email;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public static User create(String id, String email, String birthDate, Gender gender) {
        User user = User.builder()
                .id(id)
                .email(email)
                .birthDate(new BirthDate(birthDate))
                .gender(gender)
                .build();

        user.validate();
        return user;
    }

}
