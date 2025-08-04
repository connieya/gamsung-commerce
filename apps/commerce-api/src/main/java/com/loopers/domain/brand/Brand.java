package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Brand {

    private Long id;
    private String name;
    private String description;

    @Builder
    private Brand(Long id, String name, String description) {
        if (!StringUtils.hasText(name)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이름이 올바르지 않습니다.");
        }

        if (!StringUtils.hasText(description)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "설명이 올바르지 않습니다.");
        }

        this.id = id;
        this.name = name;
        this.description = description;
    }

    public static Brand create(String name, String description) {
        return Brand.builder()
                .name(name)
                .description(description)
                .build();
    }
}
