package com.loopers.interfaces.api.brand;

import com.loopers.domain.brand.BrandInfo;

import java.util.List;

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

    public record BrandListResponse(
            List<BrandResponse> brands
    ) {
        public static BrandListResponse from(List<BrandInfo> brandInfos) {
            List<BrandResponse> brands = brandInfos.stream()
                    .map(BrandResponse::from)
                    .toList();
            return new BrandListResponse(brands);
        }
    }
}
