package com.loopers.domain.order;

import java.util.Optional;

public interface IssuedOrderNoRepository {
    IssuedOrderNo save(IssuedOrderNo issuedOrderNo);

    Optional<IssuedOrderNo> findByOrderNo(String orderNo);
}
