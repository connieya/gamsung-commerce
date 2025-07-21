package com.loopers.domain.user;

import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.domain.user.vo.Gender;
import jakarta.validation.ConstraintViolationException;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class UserTest {

  @DisplayName("Id 가 영문 및 숫자 10 이내 형식에 맞지 않으면. User 객체 생성에 싪패한다.")
  @ParameterizedTest
  @ValueSource(strings = {"박건희", "박건희11", "gbb__",})
  void registerFail_whenIdFormatIsInvalid(String id) {
    // given
    User user = UserFixture.complete().set(Select.field(User::getId), id).create();

    // when , then
    assertThatThrownBy(user::validate)
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("ID는 영문 및 숫자 10자 이내여야 합니다.");

  }

  @DisplayName("회원가입: 이메일이 xx@yy.zz 형식에 맞지 않으면, User 객체 생성에 실패한다..")
  @ParameterizedTest
  @ValueSource(strings = {"geonhee@", "geonhee", "geonhee@naver", "@naver.com", "geonhee@.com"})
  void registerFail_WhenEmailFormatIsInvalid(String email) {
    // given
    User user = UserFixture.complete().set(Select.field(User::getEmail), email).create();

    // when & then
    assertThatThrownBy(user::validate)
        .isInstanceOf(ConstraintViolationException.class)
        .hasMessageContaining("이메일");

  }

  @DisplayName("회원가입: 생년월일이 yyyy-MM-dd 형식에 맞지 않으면, User 객체 생성에 실패한다.")
  @ParameterizedTest
  @ValueSource(strings = {"19990909", "1994.01.01", "09-09-1990", "1994/09/19",})
  void registerFail_WhenBirthDateFormatIsInvalid(String birthDate) {
    // given
    UserFixture.UserCreateArgs args = UserFixture.createValidUserCreateArgs();

    // when & then
    assertThatThrownBy(() -> User.create(args.id, args.email, birthDate, args.gender))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("생년월일은");

  }

}
