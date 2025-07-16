package com.loopers.infrastructure.user.jpa;

import com.loopers.domain.user.User;
import com.loopers.infrastructure.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUserId(String id);
}
