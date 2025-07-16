package com.loopers.domain.user;

import com.loopers.domain.user.vo.Gender;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


import static org.assertj.core.api.Assertions.*;

class UserTest {

    @DisplayName("Id 가 영문 및 숫자 10 이내 형식에 맞지 않으면. User 객체 생성에 싪패한다.")
    @ParameterizedTest
    @ValueSource(strings = {
            "박건희",
            "박건희11",
            "gbb__",
    })
    void registerFail_whenIdFormatIsInvalid(String id) {
        // given
        String email = "박건희11";
        String birthDay = "1994-09-26";

        // when & then
        assertThatThrownBy(() -> User.create(id, email, birthDay, Gender.MALE))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("ID는 영문 및 숫자 10자 이내여야 합니다.");

    }


    @Test
    @DisplayName("회원가입: 이메일이 xx@yy.zz 형식에 맞지 않으면, User 객체 생성에 실패한다..")
    void registerFail_() {
        // given
        String id = "geonhee";
        String email = "geonhee@";
        String birthDay = "1994-09-26";

        // when & then
        assertThatThrownBy(() -> User.create(id, email, birthDay, Gender.MALE))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("이메일");

    }

    @Test
    @DisplayName("회원가입: 생년월일이 yyyy-MM-dd 형식에 맞지 않으면, User 객체 생성에 실패한다.")
    void registerFail_2() {
        // given
        String id = "geonhee";
        String email = "geonhee@naver.com";
        String birthDay = "940926";

        // when & then
        assertThatThrownBy(() -> User.create(id, email, birthDay, Gender.MALE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("생년월일은");

    }


}
