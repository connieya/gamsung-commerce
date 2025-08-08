package com.loopers.infrastructure.point;


import com.loopers.domain.point.PointRepository;
import com.loopers.domain.point.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {

    private final PointJpaRepository pointJpaRepository;


    @Override
    public Optional<Point> findByUserId(String userId) {
        return pointJpaRepository.findByUserId(userId).map(PointEntity::toDomain);
    }

    @Override
    public Optional<Point> findPointForUpdate(String userId) {
        return pointJpaRepository.findByUserIdForUpdate(userId).map(PointEntity::toDomain);
    }

    @Override
    @Transactional
    public Point save(Point point) {
        if (point.getId() != null) {
            Optional<PointEntity> pointEntity = pointJpaRepository.findById(point.getId());
            if (pointEntity.isPresent()) {
                PointEntity entityToUpdate = pointEntity.get();
                entityToUpdate.changeValue(point.getValue());
                // 여기서 save()를 다시 호출하여 영속성 컨텍스트에 업데이트를 반영합니다.
                 pointJpaRepository.save(entityToUpdate);
                 return point;
            }
        }
        // ID가 없거나 기존 엔티티를 찾지 못하면, 새로운 엔티티를 생성합니다.
        return pointJpaRepository.save(PointEntity.from(point)).toDomain();
    }
}
