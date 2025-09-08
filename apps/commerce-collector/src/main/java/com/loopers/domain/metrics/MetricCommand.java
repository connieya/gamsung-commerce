package com.loopers.domain.metrics;

import java.util.List;

public record MetricCommand() {

    public record Aggregate(
        List<Item> items
    ) {
        public record Item(

        ){

        }
    }
}
