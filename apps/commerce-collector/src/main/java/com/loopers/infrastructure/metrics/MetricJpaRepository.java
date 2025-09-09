package com.loopers.infrastructure.metrics;

import com.loopers.domain.metrics.ProductMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public interface MetricJpaRepository extends JpaRepository<ProductMetrics, Long> {

    @Modifying
    @Query("insert into ProductMetrics (date,likeCount,saleQuantity,viewCount,productId, createdAt, updatedAt)" +
            "values (:date, :likeCount, :saleQuantity, :viewCount , :productId , :createdAt, :updatedAt) " +
            "on conflict (date, productId) do update set " +
            "likeCount = likeCount + :likeCount , saleQuantity = saleQuantity + :saleQuantity," +
            "viewCount = viewCount + :viewCount , updatedAt = :updatedAt")
    int upsert(
            @Param("date") LocalDate date,
            @Param("likeCount") Long likeCount,
            @Param("saleQuantity") Long saleQuantity,
            @Param("viewCount") Long viewCount,
            @Param("productId") Long productId,
            @Param("createdAt") ZonedDateTime createdAt,
            @Param("updatedAt") ZonedDateTime updatedAt
    );

}
