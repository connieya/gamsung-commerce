package com.loopers.application.likes;

import com.loopers.domain.brand.BrandInfo;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.likes.ProductLike;
import com.loopers.domain.likes.ProductLikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductDetailInfo;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class LikeProductFacade {

    private final UserService userService;
    private final ProductLikeService productLikeService;
    private final ProductService productService;
    private final BrandService brandService;

    @Transactional(readOnly = true)
    public GetLikeProductResult getLikedProducts(String userId) {
        User user = userService.findByUserId(userId);
        List<ProductLike> productLikes = productLikeService.findByUserId(user.getId());
        List<Long> productsId = productLikes.stream()
                .map(ProductLike::getProductId)
                .toList();
        List<Product> products = productService.findAllById(productsId);
        Map<Long, BrandInfo> brandMap = brandService.findAllById(
                products.stream().map(Product::getBrandId).toList()
        ).stream().collect(Collectors.toMap(BrandInfo::id, brand -> brand));

        List<ProductDetailInfo> productDetailInfos = products.stream()
                .map(product -> {
                    BrandInfo brand = brandMap.get(product.getBrandId());
                    Long likeCount = productLikeService.getLikeCount(product.getId());
                    return ProductDetailInfo.create(product.getId(), product.getName(), product.getPrice(), brand.name(), brand.id(), product.getImageUrl(), likeCount);
                }).toList();

        return GetLikeProductResult.create(productDetailInfos);
    }

}
