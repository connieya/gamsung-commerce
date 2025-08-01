package com.loopers.domain.product.fixture;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.brand.Brand;
import org.instancio.Instancio;
import org.instancio.InstancioApi;
import org.instancio.Select;

import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ProductFixture {

    public static InstancioApi<Product> complete() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault()); // 시스템 기본 타임존 사용
        ZonedDateTime thirtyDaysAgo = now.minusDays(30);

        return Instancio.of(Product.class)
                .generate(Select.field(Product::getName), generators -> generators.string().length(2,20))
                .generate(Select.field(Product::getPrice), generators -> generators.longs().range(1000L,5000_000L))
                .generate(Select.field(Product::getReleasedAt), generators ->
                        generators.temporal().zonedDateTime().range(thirtyDaysAgo, now)
                );
    }
}
