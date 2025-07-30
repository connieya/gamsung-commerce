package com.loopers.domain.likes;

import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
public class ProductLike {
    private User user;
    private Product product;

    @Builder
    private ProductLike(User user, Product product) {
        this.user = user;
        this.product = product;
    }

    public static ProductLike create(User user, Product product) {
        ProductLike productLike = new ProductLike();

        productLike.user = user;
        productLike.product = product;

        return productLike;
    }


}
