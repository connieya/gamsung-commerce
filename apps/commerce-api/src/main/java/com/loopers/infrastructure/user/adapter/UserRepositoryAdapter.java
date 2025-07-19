package com.loopers.infrastructure.user.adapter;

import com.loopers.application.user.port.out.UserRepositoryOut;
import com.loopers.domain.user.User;
import com.loopers.infrastructure.user.entity.UserEntity;
import com.loopers.infrastructure.user.jpa.UserJpaRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryAdapter implements UserRepositoryOut {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findByUserId(String id) {
        return userJpaRepository.findByUserId(id)
                .map(UserEntity::toDomain);
    }

    @Override
    public User save(User user) {
        System.out.println(">>> UserRepositoryAdapter.save() 실제 로직 실행됨! <<<");
        return userJpaRepository.save(UserEntity.fromDomain(user)).toDomain();
    }
}
