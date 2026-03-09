package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductDetailInfo;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.brand.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    List<Product> findByBrand(Brand brand);

    @Query("SELECT new com.loopers.domain.product.ProductInfo(" +
            "p.id, " +
            "p.price, " +
            "p.name, " +
            "b.name, " +
            "p.imageUrl, " +
            "s.likeCount, " +
            "p.releasedAt" +
            ") FROM Product p " +
            "LEFT JOIN p.brand b " +
            "LEFT JOIN LikeSummary s on s.target.id = p.id and s.target.type = 'PRODUCT'"
    )
    Page<ProductInfo> findProductDetails(Pageable pageable);

    @Query("SELECT new com.loopers.domain.product.ProductInfo(" +
            "p.id, " +
            "p.price, " +
            "p.name, " +
            "b.name, " +
            "p.imageUrl, " +
            "s.likeCount, " +
            "p.releasedAt" +
            ") FROM Product p " +
            "LEFT JOIN p.brand b " +
            "LEFT JOIN LikeSummary s on s.target.id = p.id and s.target.type = 'PRODUCT'"
    )
    Page<ProductInfo> findProductDetailsOptimized(Pageable pageable);


    @Query("SELECT new com.loopers.domain.product.ProductInfo(" +
            "p.id, " +
            "p.price, " +
            "p.name, " +
            "b.name, " +
            "p.imageUrl, " +
            "s.likeCount, " +
            "p.releasedAt" +
            ") FROM Product p " +
            "LEFT JOIN p.brand b " +
            "LEFT JOIN LikeSummary s on s.target.id = p.id and s.target.type = 'PRODUCT'" +
            "WHERE (:brandId IS NULL OR b.id = :brandId)"
    )
    Page<ProductInfo> findProductDetailsDenormalizedLikeCount(Pageable pageable , Long brandId);



    @Query("SELECT new com.loopers.domain.product.ProductInfo(" +
            "p.id, " +
            "p.price, " +
            "p.name, " +
            "b.name, " +
            "p.imageUrl, " +
            "s.likeCount, " +
            "p.releasedAt" +
            ") FROM LikeSummary s " +
            "JOIN Product p ON s.target.id = p.id " +
            "LEFT JOIN p.brand b " +
            "WHERE s.target.type = 'PRODUCT'"
    )
    Page<ProductInfo> findProductDetailsDenormalizedLikeCountOptimized(Pageable pageable);


    @Query("SELECT new com.loopers.domain.product.ProductInfo(" +
            "p.id, " +
            "p.price, " +
            "p.name, " +
            "b.name, " +
            "p.imageUrl, " +
            "s.likeCount, " +
            "p.releasedAt" +
            ") FROM Product p " +
            "LEFT JOIN p.brand b " +
            "LEFT JOIN LikeSummary s on s.target.id = p.id and s.target.type = 'PRODUCT' " +
            "WHERE p.id in :rankingInfo"
    )
    List<ProductInfo> findRankByIds(@Param("rankingInfo") List<Long> rankingInfo);

    @Query("SELECT new com.loopers.domain.product.ProductInfo(" +
            "p.id, " +
            "p.price, " +
            "p.name, " +
            "b.name, " +
            "p.imageUrl, " +
            "s.likeCount, " +
            "p.releasedAt" +
            ") FROM Product p " +
            "LEFT JOIN p.brand b " +
            "LEFT JOIN LikeSummary s on s.target.id = p.id and s.target.type = 'PRODUCT' " +
            "WHERE p.categoryId = :categoryId"
    )
    Page<ProductInfo> findByCategoryId(Pageable pageable, @Param("categoryId") Long categoryId);

    @Query("SELECT new com.loopers.domain.product.ProductDetailInfo(" +
            "p.id," +
            "p.name," +
            "p.price," +
            "b.name," +
            "b.id, " +
            "p.imageUrl, " +
            "l.likeCount" +
            ") from Product p " +
            "left join p.brand b " +
            "left join LikeSummary l on p.id = l.target.id " +
            "where p.id = :productId"
    )
    Optional<ProductDetailInfo> findProductDetail(Long productId);
}
