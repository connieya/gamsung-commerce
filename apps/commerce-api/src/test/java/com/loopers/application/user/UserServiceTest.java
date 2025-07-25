package com.loopers.application.user;

import com.loopers.application.user.exception.UserException;
import com.loopers.application.user.port.in.UserInfoResult;
import com.loopers.application.user.port.in.UserRegisterCommand;
import com.loopers.domain.user.User;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.domain.user.fixture.UserRegisterCommandFixture;
import com.loopers.domain.user.vo.Gender;
import com.loopers.infrastructure.user.adapter.UserRepositoryAdapter;
import com.loopers.infrastructure.user.entity.UserEntity;
import com.loopers.infrastructure.user.jpa.UserJpaRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.N;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserJpaRepository userJpaRepository;

    private UserRepositoryAdapter userRepositoryAdapter;

    @BeforeEach
    void setUp() {
        userRepositoryAdapter = spy(new UserRepositoryAdapter(userJpaRepository));
        userService = new UserService(userRepositoryAdapter);
    }


    @Nested
    @DisplayName("회원 가입")
    class register {
        @Test
        @DisplayName("이미 가입된 ID 로 회원가입 시도 시, 실패한다.")
        void registerFail() {
            // given
            UserRegisterCommand command = UserRegisterCommandFixture.complete().create();


            doReturn(Optional.of(User.create(command.getUserId(), command.getEmail(), command.getBirthDate(), command.getGender())))
                    .when(userRepositoryAdapter).findByUserId(command.getUserId());

            // when & then
            Assertions.assertThatThrownBy(() -> {
                userService.register(command);
            }).isInstanceOf(UserException.UserAlreadyExistsException.class);

        }


        @Test
        @DisplayName("회원 가입시 User 저장이 수행된다.")
        void registerSuccess() {
            // given
            UserRegisterCommand command = UserRegisterCommandFixture.complete().create();
            User user = User.create(command.getUserId(), command.getEmail(), command.getBirthDate(), command.getGender());

            doReturn(UserEntity.fromDomain(user))
                    .when(userJpaRepository).save(any(UserEntity.class));
            // when
            userService.register(command);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            // then
            verify(userRepositoryAdapter, times(1)).save(userCaptor.capture());
        }
    }

    @Nested
    @DisplayName("내 정보 조회")
    class getUser {
        @Test
        @DisplayName("해당 ID 의 회원이 존재할 경우, 회원 정보가 반환된다.")
        void getUserSuccess() {
            // given
            User user = UserFixture.complete().create();

            // when
            doReturn(Optional.of(user)).when(userRepositoryAdapter).findByUserId(user.getId());
            UserInfoResult userInfoResult = userService.getUser(user.getId());

            // then
            assertThat(userInfoResult.getUserId()).isEqualTo(user.getId());
            assertThat(userInfoResult.getEmail()).isEqualTo(user.getEmail());
            assertThat(userInfoResult.getBirthdate()).isEqualTo(user.getBirthDate().getBirthDate());

        }

        @Test
        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, 예외가 발생한다")
        void getUserFail() {
            // given
            String userId = "geonhee77";

            // when
            doReturn(Optional.empty()).when(userRepositoryAdapter).findByUserId(userId);

            // then
            Assertions.assertThatThrownBy(() -> {
                userService.getUser(userId);
            }).isInstanceOf(UserException.UserNotFoundException.class);
        }
    }

}
