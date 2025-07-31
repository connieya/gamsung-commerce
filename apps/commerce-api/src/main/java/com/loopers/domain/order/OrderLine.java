package com.loopers.domain.order;

import lombok.Getter;

@Getter
public class OrderLine {

    private Long productId;
    private Long orderId;
    private Long quantity;
    private Long price;
}
