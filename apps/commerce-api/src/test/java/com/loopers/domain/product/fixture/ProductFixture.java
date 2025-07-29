package com.loopers.domain.product.fixture;

import com.loopers.domain.product.Product;
import org.instancio.Instancio;
import org.instancio.InstancioApi;
import org.instancio.Select;

public class ProductFixture {

    public static InstancioApi<Product> complete() {
        return Instancio.of(Product.class)
                .generate(Select.field(Product::getName), generators -> generators.string().length(2,20))
                .generate(Select.field(Product::getPrice), generators -> generators.longs().range(1000L,5000_000L))
                .supply(Select.field(Product::getBrand), ()-> BrandFixture.complete().create());
    }
}
