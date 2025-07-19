package com.loopers.infrastructure.user;

import com.loopers.application.user.port.out.UserRepositoryOut;
import com.loopers.domain.user.User;
import com.loopers.infrastructure.user.entity.UserEntity;

import java.util.*;

public class FakeUserRepository2 implements UserRepositoryOut {

    private final Map<String, UserEntity> entityStorage = new HashMap<>();

    @Override
    public User save(User user) {
        // Fake 내부에서도 실제 구현체처럼 매핑 로직을 수행!
        UserEntity entity = UserEntity.fromDomain(user); // 여기서 버그가 실행됨!
        entityStorage.put(entity.getUserId(), entity);
        return entity.toDomain();
    }


    @Override
    public Optional<User> findByUserId(String userId) {
        // 내부 저장소(Entity 형태)에서 데이터를 찾는다.
        UserEntity foundEntity = entityStorage.get(userId);

        // 찾은 Entity를 도메인 객체로 변환하여 반환한다.
        // Optional.ofNullable을 사용하여 null일 경우 Optional.empty()가 반환되도록 한다.
        return Optional.ofNullable(foundEntity).map(UserEntity::toDomain);
    }

}
