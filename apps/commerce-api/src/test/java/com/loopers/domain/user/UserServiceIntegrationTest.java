package com.loopers.domain.user;

import com.loopers.domain.user.exception.UserException;
import com.loopers.domain.user.vo.Gender;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceIntegrationTest {

    @Autowired
    UserService userService;

    @MockitoSpyBean
    UserRepository userRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("회원 가입")
    @Nested
    class Register {

        @DisplayName("이미 가입된 ID 로 회원가입 시도 시, 실패한다.")
        @Test
        void registerFailWhenUserAlreadyExists() {
            // given
            String userId = "testUser";

            doReturn(Optional.of(User.create(userId, "dd@naver.com", "1999-09-09", Gender.FEMALE))).when(userRepository).findByUserId(userId);

            // when , then
            assertThatThrownBy(() -> {
                userService.register(UserRegisterCommand.of(userId, "geonhee@naver.com", "1994-09-26", Gender.MALE));
            }).isInstanceOf(UserException.UserAlreadyExistsException.class);

        }

        @DisplayName("회원 가입시 User 저장이 수행된다.")
        @Test
        void registerSuccess() {
            // given
            String userId = "testUser";

            // when
            userService.register(UserRegisterCommand.of(userId, "geonhee@naver.com", "1994-09-26", Gender.MALE));

            // then
            verify(userRepository, times(1)).save(any(User.class));

        }
    }

}
