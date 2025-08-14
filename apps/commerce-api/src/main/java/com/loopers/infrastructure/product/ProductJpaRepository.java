package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.brand.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findByBrand(Brand brand);

    @Query("SELECT new com.loopers.domain.product.ProductInfo(" +
            "p.id," +
            "p.price," +
            "p.name," +
            "b.name," +
            "cast(count(pl.id) as long) as likeCount," + // 좋아요 수
            "p.releasedAt" +
            ") from ProductEntity p " +
            "left join p.brand b " +
            "left join ProductLike  pl on p.id = pl.productId " +
            "group by p.id, p.price, p.name, b.name, p.releasedAt "
    )
    Page<ProductInfo> findProductDetails(Pageable pageable);

    @Query("SELECT new com.loopers.domain.product.ProductInfo(" +
            "p.id, " +
            "p.price, " +
            "p.name, " +
            "b.name, " +
            "cast((SELECT COUNT(l) FROM ProductLike l WHERE l.productId = p.id) as long )as likeCount, " +
            "p.releasedAt" +
            ") FROM ProductEntity p " +
            "LEFT JOIN p.brand b " +
            "WHERE b.id = :brandId"
    )
    Page<ProductInfo> findProductDetailsOptimized(Pageable pageable , Long brandId);


    @Query("SELECT new com.loopers.domain.product.ProductInfo(" +
            "p.id, " +
            "p.price, " +
            "p.name, " +
            "b.name, " +
            "s.likeCount, " +
            "p.releasedAt" +
            ") FROM ProductEntity p " +
            "LEFT JOIN p.brand b " +
            "LEFT JOIN LikeSummary s on s.target.id = p.id and s.target.type = 'PRODUCT'" +
            "WHERE b.id = :brandId"
    )
    Page<ProductInfo> findProductDetailsDenormalizedLikeCount(Pageable pageable , Long brandId);


    @Query("SELECT new com.loopers.domain.product.ProductInfo(" +
            "p.id, " +
            "p.price, " +
            "p.name, " +
            "b.name, " +
            "s.likeCount, " +
            "p.releasedAt" +
            ") FROM ProductEntity p " +
            "LEFT JOIN p.brand b " +
            "LEFT JOIN LikeSummary s ON s.target.id = p.id AND s.target.type = 'PRODUCT' "
    )
    Page<ProductInfo> findProductDetailsDenormalizedLikeCount(Pageable pageable);


    @Query("SELECT new com.loopers.domain.product.ProductInfo(" +
            "p.id, " +
            "p.price, " +
            "p.name, " +
            "b.name, " +
            "s.likeCount, " +
            "p.releasedAt" +
            ") FROM LikeSummary s " +
            "JOIN ProductEntity p ON s.target.id = p.id " +
            "LEFT JOIN p.brand b " +
            "WHERE s.target.type = 'PRODUCT'"
    )
    Page<ProductInfo> findProductDetailsDenormalizedLikeCountOptimized(Pageable pageable);
}
