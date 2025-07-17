package com.loopers.infrastructure.user;

import com.loopers.application.user.port.out.UserRepositoryOut;
import com.loopers.domain.user.User;

import java.util.Optional;

public class FakeUserRepository implements UserRepositoryOut {
    @Override
    public Optional<User> findByUserId(String id) {
        return Optional.empty();
    }

    @Override
    public User save(User user) {
        return user;
    }
}
