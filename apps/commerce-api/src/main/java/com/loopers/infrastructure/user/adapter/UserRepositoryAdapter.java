package com.loopers.infrastructure.user.adapter;

import com.loopers.application.user.port.out.UserRepositoryOut;
import com.loopers.domain.user.User;
import com.loopers.infrastructure.user.entity.UserEntity;
import com.loopers.infrastructure.user.jpa.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryOut {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findByUserId(String id) {
        return userJpaRepository.findByUserId(id)
                .map(UserEntity::toDomain);
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(UserEntity.fromDomain(user)).toDomain();
    }
}
