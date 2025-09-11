package com.loopers.application.product;

import com.loopers.domain.activity.event.ActivityEvent;
import com.loopers.domain.product.ProductDetailInfo;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.rank.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;
    private final RankingService rankingService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ProductResult getProductDetail(ProductCriteria.GetDetail productCriteria) {
        applicationEventPublisher.publishEvent(ActivityEvent.View.from(productCriteria.productId()));

        ProductDetailInfo productDetail = productService.getProductDetail(productCriteria.productId());
        return ProductResult.of(
                productDetail.getProductId(),
                productDetail.getProductName(),
                productDetail.getProductPrice(),
                productDetail.getBrandName(),
                productDetail.getBrandId(),
                productDetail.getLikeCount()
        );
    }

}
