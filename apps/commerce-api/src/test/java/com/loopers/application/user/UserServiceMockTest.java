package com.loopers.application.user;

import com.loopers.application.user.exception.UserException;
import com.loopers.application.user.port.in.UserInfoResult;
import com.loopers.application.user.port.in.UserRegisterCommand;
import com.loopers.application.user.port.in.UserRegisterResult;
import com.loopers.application.user.port.out.UserRepositoryOut;
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
class UserServiceMockTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepositoryOut userRepositoryOut;


    @Test
    @DisplayName("회원 가입시 User 저장이 수행된다.")
    void registerSuccess() {
        // given
        String userId = "geonhee77";
        String email = "geonhee77@naver.com";
        String birthDate = "2020-01-01";
        Gender gender = Gender.MALE;
        // given
        UserRegisterCommand command = UserRegisterCommand.of(userId, email, birthDate, gender);
        User savedUser = User.create(userId, email, birthDate, gender);

        when(userRepositoryOut.save(any(User.class))).thenReturn(savedUser);

        // when
        UserRegisterResult result = userService.register(command);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryOut, times(1)).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getId()).isEqualTo(userId);
        assertThat(capturedUser.getEmail()).isEqualTo(email);

        assertThat(result.id()).isEqualTo(savedUser.getId());
        assertThat(result.email()).isEqualTo(email);
    }
}
