package com.loopers.domain.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryInfo> getCategoryTree() {
        List<Category> allCategories = categoryRepository.findAll();

        Map<Long, List<Category>> childrenByParentId = allCategories.stream()
                .filter(category -> !category.isRoot())
                .collect(Collectors.groupingBy(Category::getParentId));

        return allCategories.stream()
                .filter(Category::isRoot)
                .sorted(Comparator.comparingInt(Category::getDisplayOrder))
                .map(root -> CategoryInfo.of(
                        root,
                        toChildInfos(childrenByParentId, root.getId())
                ))
                .toList();
    }

    private List<CategoryInfo> toChildInfos(Map<Long, List<Category>> childrenByParentId, Long parentId) {
        List<Category> children = childrenByParentId.getOrDefault(parentId, List.of());
        return children.stream()
                .sorted(Comparator.comparingInt(Category::getDisplayOrder))
                .map(child -> CategoryInfo.of(
                        child,
                        toChildInfos(childrenByParentId, child.getId())
                ))
                .toList();
    }
}
