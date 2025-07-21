package com.loopers.application.user;

import com.loopers.application.user.exception.UserException;
import com.loopers.application.user.port.in.UserInfoResult;
import com.loopers.application.user.port.in.UserRegisterCommand;
import com.loopers.domain.user.User;
import com.loopers.domain.user.vo.Gender;
import com.loopers.infrastructure.user.adapter.UserRepositoryAdapter;
import com.loopers.infrastructure.user.entity.UserEntity;
import com.loopers.infrastructure.user.jpa.UserJpaRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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


    @Test
    @DisplayName("이미 가입된 ID 로 회원가입 시도 시, 실패한다.")
    void registerFail() {
        // given
        String id = "geonhee77";
        String email = "geonhee77@naver.com";
        String birthDate = "1990-01-01";
        Gender gender = Gender.MALE;

        UserRegisterCommand userRegisterCommand = UserRegisterCommand.of(id, email, birthDate, gender);

        doReturn(Optional.of(User.create(id, email, birthDate, gender))).when(userRepositoryAdapter).findByUserId(id);
        // when & then
        Assertions.assertThatThrownBy(() -> {
            userService.register(userRegisterCommand);
        }).isInstanceOf(UserException.UserAlreadyExistsException.class);

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

        doReturn(UserEntity.fromDomain(User.create(id, email, birthDate, gender))).when(userJpaRepository).save(any(UserEntity.class));
        // when
        userService.register(userRegisterCommand);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        // then
        verify(userRepositoryAdapter, times(1)).save(userCaptor.capture());
    }

    @Test
    @DisplayName("해당 ID 의 회원이 존재할 경우, 회원 정보가 반환된다.")
    void getUserSuccess() {
        // given
        String userId = "geonhee77";

        // when
        doReturn(Optional.of(User.create(userId, "geonhee77@naver.com", "1994-09-26", Gender.MALE))).when(userRepositoryAdapter).findByUserId(userId);
        UserInfoResult user = userService.getUser(userId);

        // then
        assertThat(user.getUserId()).isEqualTo(userId);
        assertThat(user.getEmail()).isEqualTo("geonhee77@naver.com");
        assertThat(user.getBirthdate()).isEqualTo(LocalDate.of(1994,9,26));

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
