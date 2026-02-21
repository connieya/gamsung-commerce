package com.loopers.interfaces.api.brand;

import com.loopers.domain.brand.BrandInfo;

import java.util.List;

public class BrandV1Dto {
    public static class Response {
        public record Brand(
                Long id,
                String name,
                String description
        ) {

            public static Brand from(BrandInfo brandInfo){
                return new Brand(
                        brandInfo.id(),
                        brandInfo.name(),
                        brandInfo.description()
                );
            }

        }

        public record BrandList(
                List<Brand> brands
        ) {
            public static BrandList from(List<BrandInfo> brandInfos) {
                List<Brand> brands = brandInfos.stream()
                        .map(Brand::from)
                        .toList();
                return new BrandList(brands);
            }
        }
    }
}
