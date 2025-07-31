package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    @Query("SELECT new com.loopers.domain.product.ProductInfo(" +
            "p.id," +
            " p.price," +
            "p.name," +
            "b.name" +
            ",count(pl.id)" +
            ",p.createdAt" +
            ") from ProductEntity p " +
            "left join p.brandEntity b " +
            "left join ProductLikeEntity  pl on p.id = pl.productEntity.id " +
            "group by p.id,p.price,p.name,b.name , p.createdAt")
    Page<ProductInfo> findProductDetails(Pageable pageable);
}
