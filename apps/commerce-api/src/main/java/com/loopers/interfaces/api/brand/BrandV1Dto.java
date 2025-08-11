package com.loopers.interfaces.api.brand;

import com.loopers.domain.brand.BrandInfo;

public class BrandV1Dto {
    public record BrandResponse(
            Long id,
            String name,
            String description
    ) {

        public static BrandResponse from(BrandInfo brandInfo){
            return new BrandResponse(
                    brandInfo.id(),
                    brandInfo.name(),
                    brandInfo.description()
            );
        }

    }
}
