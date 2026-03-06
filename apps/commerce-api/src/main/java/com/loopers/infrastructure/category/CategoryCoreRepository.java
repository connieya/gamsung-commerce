package com.loopers.infrastructure.category;

import com.loopers.domain.category.Category;
import com.loopers.domain.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Repository
public class CategoryCoreRepository implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public List<Category> findAll() {
        return StreamSupport.stream(categoryJpaRepository.findAll().spliterator(), false)
                .toList();
    }

    @Override
    public Optional<Category> findById(Long categoryId) {
        return categoryJpaRepository.findById(categoryId);
    }
}
