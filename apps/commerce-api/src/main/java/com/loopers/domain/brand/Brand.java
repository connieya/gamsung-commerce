package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Entity
@Table(name = "brand")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Brand extends BaseEntity {

    private String name;
    private String description;

    @Builder
    private Brand(String name, String description) {
        if (!StringUtils.hasText(name)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이름이 올바르지 않습니다.");
        }

        if (!StringUtils.hasText(description)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "설명이 올바르지 않습니다.");
        }
        this.name = name;
        this.description = description;
    }

    public static Brand create(String name, String description) {
        return Brand.builder()
                .name(name)
                .description(description)
                .build();
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
