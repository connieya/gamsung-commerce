package com.loopers.application.user;

import com.loopers.application.user.port.in.UserRegisterCommand;
import com.loopers.application.user.port.in.UserRegisterResult;
import com.loopers.domain.user.vo.Gender;
import com.loopers.infrastructure.user.FakeUserRepository;
import com.loopers.infrastructure.user.FakeUserRepository2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


@ExtendWith(MockitoExtension.class)
class UserServiceFakeTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        FakeUserRepository fakeUserRepository = new FakeUserRepository();
        userService = new UserService(fakeUserRepository);
    }

    @Test
    @DisplayName("회원 가입시 User 저장이 수행된다.")
    void registerSuccess() {
        // given
        String id = "geonhee77";
        String email = "geonhee77@naver.com";
        String birthDate = "1990-01-01";
        Gender gender = Gender.MALE;
        UserRegisterCommand userRegisterCommand = UserRegisterCommand.of(id, email, birthDate, gender);

        // when
        UserRegisterResult userRegisterResult = userService.register(userRegisterCommand);

        // then
        assertAll(() -> {
            assertThat(userRegisterResult.id()).isEqualTo(id);
            assertThat(userRegisterResult.email()).isEqualTo(email);
            assertThat(userRegisterResult.birthDate()).isEqualTo(birthDate);
        });

    }

}


