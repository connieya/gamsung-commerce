package com.loopers.domain.category;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false)
    private int depth;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Builder
    private Category(String name, Long parentId, int depth, int displayOrder) {
        if (!StringUtils.hasText(name)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "카테고리 이름은 필수입니다.");
        }
        if (depth < 1) {
            throw new CoreException(ErrorType.BAD_REQUEST, "depth는 1 이상이어야 합니다.");
        }
        if (depth == 1 && parentId != null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "최상위 카테고리는 parentId를 가질 수 없습니다.");
        }
        if (depth > 1 && parentId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "하위 카테고리는 parentId가 필수입니다.");
        }

        this.name = name;
        this.parentId = parentId;
        this.depth = depth;
        this.displayOrder = displayOrder;
    }

    public static Category createRoot(String name, int displayOrder) {
        return Category.builder()
                .name(name)
                .parentId(null)
                .depth(1)
                .displayOrder(displayOrder)
                .build();
    }

    public static Category createChild(String name, Category parent, int displayOrder) {
        return Category.builder()
                .name(name)
                .parentId(parent.getId())
                .depth(parent.getDepth() + 1)
                .displayOrder(displayOrder)
                .build();
    }

    public boolean isRoot() {
        return this.parentId == null;
    }
}
