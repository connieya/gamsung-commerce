package com.loopers.infrastructure.product.option;

import com.loopers.domain.product.option.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductOptionJpaRepository extends JpaRepository<ProductOption, Long> {

    List<ProductOption> findByProductId(Long productId);
}
