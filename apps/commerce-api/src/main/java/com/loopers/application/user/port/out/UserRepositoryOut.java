package com.loopers.application.user.port.out;

import com.loopers.domain.user.User;

import java.util.Optional;

public interface UserRepositoryOut {

    Optional<User> findByUserId(String id);

    User save(User user);
}
