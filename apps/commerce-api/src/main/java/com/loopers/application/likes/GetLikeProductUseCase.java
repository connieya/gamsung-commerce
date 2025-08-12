package com.loopers.application.likes;

import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.likes.ProductLikeRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductDetailInfo;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.likes.ProductLike;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class GetLikeProductUseCase {

    private final UserService userService;
    private final ProductLikeRepository productLikeRepository;
    private final BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public GetLikeProductResult getLikedProducts(String userId) {
        User user = userService.findByUserId(userId);
        List<ProductLike> productLikes = productLikeRepository.findByUserId(user.getId());
        List<Product> products = productLikes.stream()
                .map(productLikeEntity -> productLikeEntity.getProductEntity().toDomain())
                .toList();
        List<Brand> brands = brandRepository.findAllById(products.stream().map(Product::getBrandId).toList());
        Map<Long, Brand> brandMap = brands.stream()
                .collect(Collectors.toMap(Brand::getId, brand -> brand));


        List<ProductDetailInfo> productDetailInfos = products.stream()
                .map(product -> {
                    Brand brand = brandMap.get(product.getBrandId());
                    Long likeCount = productLikeRepository.getLikeCount(product.getId());
                    return ProductDetailInfo.create(product.getId(), product.getName(), product.getPrice(), brand.getName(), likeCount);
                }).toList();

        return GetLikeProductResult.create(productDetailInfos);

    }

}
