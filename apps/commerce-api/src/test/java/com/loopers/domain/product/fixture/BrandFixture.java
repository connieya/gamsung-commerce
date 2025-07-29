package com.loopers.domain.product.fixture;

import com.loopers.domain.product.Brand;
import org.instancio.Instancio;
import org.instancio.InstancioApi;
import org.instancio.Select;

public class BrandFixture {

    public static InstancioApi<Brand> complete() {
        return Instancio.of(Brand.class)
                .generate(Select.field(Brand::getName) , generators -> generators.string().length(2,20))
                .generate(Select.field(Brand::getDescription) , generators -> generators.string().length(4,50));
    }
}
