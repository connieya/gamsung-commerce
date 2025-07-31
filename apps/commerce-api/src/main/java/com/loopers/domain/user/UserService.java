package com.loopers.domain.user;

import com.loopers.domain.user.exception.UserException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserRegisterResult register(UserRegisterCommand command) {
        userRepository.findByUserId(command.getUserId())
                .ifPresent(user -> {
                    throw new UserException.UserAlreadyExistsException(ErrorType.USER_ALREADY_EXISTS);
                });

        User user = userRepository.save(User.create(command.getUserId(), command.getEmail(), command.getBirthDate(), command.getGender()));

        return UserRegisterResult.of(user.getUserId(), user.getBirthDate().getBirthDate(), user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoResult getUser(String userId) {
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new UserException.UserNotFoundException(ErrorType.USER_NOT_FOUND));

        return UserInfoResult.of(user.getUserId(), user.getEmail(), user.getBirthDate().getBirthDate(), user.getGender());
    }
}
