package com.loopers.domain.likes;

import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ProductLike {
    private User user;
    private Product product;


    public static ProductLike create(User user, Product product) {
        ProductLike productLike = new ProductLike();

        productLike.user = user;
        productLike.product = product;

        return productLike;
    }


}
