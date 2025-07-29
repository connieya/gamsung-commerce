package com.loopers.domain.likes;

import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;

public interface ProductLikeRepository {

    ProductLike save(ProductLike productLike);

    boolean existsByUserIdAndProductId(User user, Product product);
}
