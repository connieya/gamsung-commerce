package com.loopers.interfaces.consumer.payment;

import java.util.List;

public record PaymentEvent( List<Item> orderLines) {

    public record Item(
            Long productId
    ) {

    }
}
