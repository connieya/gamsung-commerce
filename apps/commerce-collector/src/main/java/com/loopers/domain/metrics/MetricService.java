package com.loopers.domain.metrics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetricService {

    private final MetricRepository metricRepository;

    @Transactional
    public void aggregate(MetricCommand.Aggregate command) {
        List<MetricCommand.Aggregate.Item> items = command.items();

        List<ProductMetrics> productMetrics = items.stream()
                .map(item ->
                        ProductMetrics
                                .builder()
                                .date(item.date())
                                .productId(item.productId())
                                .viewCount(item.viewCount())
                                .likeCount(item.likeCount())
                                .saleQuantity(item.saleQuantity())
                                .build()
                ).toList();

        productMetrics
                .forEach(metricRepository::upsert);

    }

}
