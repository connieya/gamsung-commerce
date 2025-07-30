package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUserId(String id);

    User save(User user);

    Optional<User> findById(Long userId);
}
