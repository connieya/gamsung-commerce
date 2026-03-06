package com.loopers.infrastructure.category;

import com.loopers.domain.category.Category;
import org.springframework.data.repository.CrudRepository;

public interface CategoryJpaRepository extends CrudRepository<Category, Long> {
}
