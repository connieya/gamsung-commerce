package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeTargetType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikeJpaRepository extends CrudRepository<Like, Long> {

    boolean existsByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, LikeTargetType targetType);

    void deleteByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, LikeTargetType targetType);

    List<Like> findByUserIdAndTargetType(Long userId, LikeTargetType targetType);

    @Query("select l.target.id from Like l where l.userId = :userId and l.target.type = :targetType")
    List<Long> findTargetIdsByUserIdAndTargetType(@Param("userId") Long userId, @Param("targetType") LikeTargetType targetType);
}
