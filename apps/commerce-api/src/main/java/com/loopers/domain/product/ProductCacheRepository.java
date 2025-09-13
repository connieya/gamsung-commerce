package com.loopers.domain.product;

import com.loopers.domain.likes.LikeUpdateType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductCacheRepository {

    Page<ProductInfo> findProductDetails(Pageable pageable , Long brandId);

    void save(Long brandId, Page<ProductInfo> productInfoPage);

    Optional<ProductDetailInfo> findProductDetailById(Long productId);

    void saveProductDetail(ProductDetailInfo productDetailInfo);

    void updateLikeCount(Long productId, LikeUpdateType updateType);
}
