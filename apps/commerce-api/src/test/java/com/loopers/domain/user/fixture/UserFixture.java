package com.loopers.domain.user.fixture;

import com.loopers.domain.user.vo.BirthDate;
import com.loopers.domain.user.User;
import com.loopers.domain.user.vo.Gender;
import org.instancio.Instancio;
import org.instancio.InstancioApi;
import org.instancio.Select;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UserFixture {

  public static InstancioApi<User> complete() {
    return Instancio.of(User.class)
        .generate(Select.field(User::getUserId), generators -> generators.string().length(5, 10).alphaNumeric())

        .generate(Select.field(User::getEmail), generators -> generators.net().email())
        .supply(Select.field(User::getBirthDate), () -> {
          LocalDate randomDate = Instancio.of(LocalDate.class).generate(Select.root(),
              gen -> gen.temporal().localDate().range(LocalDate.of(1960, 1, 1), LocalDate.of(2010, 12, 31))).create();
          String dateString = randomDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
          return new BirthDate(dateString); // BirthDate VO의 String 생성자를 호출
        });
  }

  public static String validBirthDateString() {
    return Instancio.of(String.class).generate(Select.root(),
        gen -> gen.temporal().localDate().range(LocalDate.of(1960, 1, 1), LocalDate.of(2010, 12, 31))
            .as(date -> date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))).create();
  }

  // User.create 메서드의 인자들을 묶어주는 임시 클래스 (Test 전용)
  public static class UserCreateArgs {
    public String id;
    public String email;
    public String birthDate; // String 타입!
    public Gender gender;

    public UserCreateArgs(String id, String email, String birthDate, Gender gender) {
      this.id = id;
      this.email = email;
      this.birthDate = birthDate;
      this.gender = gender;
    }
  }


  public static UserCreateArgs createValidUserCreateArgs() {
    return new UserCreateArgs(Instancio.create(String.class), // ID는 기본 String 생성
        Instancio.of(String.class).generate(Select.root(), gen -> gen.net().email()).create(), // 이메일
        validBirthDateString(),
        Instancio.create(Gender.class) // 성별
    );
  }
}
