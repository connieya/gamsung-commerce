package com.loopers.domain.activity;

import lombok.Builder;
import lombok.Getter;

public class ActivityCommand {

    @Getter
    @Builder
    public static class View {
        private Long productId;

        public static View from(Long productId) {
            return View
                    .builder()
                    .productId(productId)
                    .build();
        }
    }
}
