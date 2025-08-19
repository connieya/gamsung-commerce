package com.loopers.domain.product.fixture;

import com.loopers.domain.brand.Brand;
import org.instancio.Instancio;
import org.instancio.InstancioApi;
import org.instancio.Select;

public class BrandFixture {

    public static InstancioApi<Brand> complete() {
        return Instancio.of(Brand.class)
                .set(Select.field(Brand::getId), null) // <-- 이 줄을 추가
                .generate(Select.field(Brand::getName) , generators -> generators.string().length(2,20))
                .generate(Select.field(Brand::getDescription) , generators -> generators.string().length(4,50));
    }
}
