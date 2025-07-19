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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserService2Test {

    @InjectMocks
    private UserService userService;

    @Spy
    private UserRepositoryAdapter userRepositoryAdapter;



    @Test
    @DisplayName("회원 가입시 User 저장이 수행된다.")
    void registerSuccess() {
        // given
        String id = "geonhee77";
        String email = "geonhee77@naver.com";
        String birthDate = "1990-01-01";
        Gender gender = Gender.MALE;
        UserRegisterCommand userRegisterCommand = UserRegisterCommand.of(id, email, birthDate, gender);

        doReturn(Optional.empty()).when(userRepositoryAdapter).findByUserId(id);
        doReturn(UserEntity.fromDomain(User.create(id, email, birthDate, gender))).when(userRepositoryAdapter).save(any(User.class));
        doReturn((User.create(id, email, birthDate, gender))).when(userRepositoryAdapter).save(any(User.class));
        // when
        userService.register(userRegisterCommand);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        // then
        verify(userRepositoryAdapter, times(1)).save(userCaptor.capture());
    }



}
