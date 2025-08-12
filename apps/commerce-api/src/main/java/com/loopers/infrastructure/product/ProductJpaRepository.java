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
            " p.price," +
            "p.name," +
            "b.name" +
            ",count(pl.id)" +
            ",p.releasedAt" +
            ") from ProductEntity p " +
            "left join p.brand b " +
            "left join ProductLike  pl on p.id = pl.productId " +
            "group by p.id,p.price,p.name,b.name , p.releasedAt")
    Page<ProductInfo> findProductDetails(Pageable pageable);
}
