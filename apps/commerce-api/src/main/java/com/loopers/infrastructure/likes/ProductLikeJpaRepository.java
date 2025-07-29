package com.loopers.infrastructure.likes;

import org.springframework.data.repository.CrudRepository;

public interface ProductLikeJpaRepository extends CrudRepository<ProductLikeEntity , Long> {
}
