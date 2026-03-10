package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeTargetType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LikeJpaRepository extends CrudRepository<Like, Long> {

    boolean existsByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, LikeTargetType targetType);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "INSERT IGNORE INTO likes (user_id, target_id, target_type, created_at, updated_at) " +
                   "VALUES (:userId, :targetId, :targetType, NOW(), NOW())",
           nativeQuery = true)
    int insertIfAbsent(@Param("userId") Long userId,
                       @Param("targetId") Long targetId,
                       @Param("targetType") String targetType);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Like l WHERE l.userId = :userId AND l.target.id = :targetId AND l.target.type = :targetType")
    int deleteByUserAndTarget(@Param("userId") Long userId,
                              @Param("targetId") Long targetId,
                              @Param("targetType") LikeTargetType targetType);

    List<Like> findByUserIdAndTargetType(Long userId, LikeTargetType targetType);

    @Query("select l.target.id from Like l where l.userId = :userId and l.target.type = :targetType")
    List<Long> findTargetIdsByUserIdAndTargetType(@Param("userId") Long userId, @Param("targetType") LikeTargetType targetType);
}
