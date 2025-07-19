package com.loopers.infrastructure.user;

import com.loopers.application.user.port.out.UserRepositoryOut;
import com.loopers.domain.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class FakeUserRepository implements UserRepositoryOut {

    private final List<User> data = new ArrayList<>();


    @Override
    public Optional<User> findByUserId(String userID) {

        return data.stream().filter(user -> user.getId().equals(userID)).findAny();
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            // 새 User인 경우 userId 생성 (UUID 사용)
            User newUser = User.builder()
                    .id(UUID.randomUUID().toString())  // 랜덤 UUID 생성
                    .email(user.getEmail())
                    .birthDate(user.getBirthDate())
                    .gender(user.getGender())
                    .build();
            data.add(newUser);
            return newUser;
        } else {
            // 기존 User인 경우 업데이트
            data.removeIf(u -> u.getId().equals(user.getId()));
            data.add(user);
            return user;
        }
    }
}
