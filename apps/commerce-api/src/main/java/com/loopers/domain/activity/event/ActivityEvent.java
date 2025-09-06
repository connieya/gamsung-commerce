package com.loopers.domain.activity.event;

import lombok.Getter;

public class ActivityEvent {

    @Getter
    public static class View {
        private Long productId;

        public View(Long productId) {
            this.productId = productId;
        }

        public static View from(Long productId){
            return new View(productId);
        }
    }
}
