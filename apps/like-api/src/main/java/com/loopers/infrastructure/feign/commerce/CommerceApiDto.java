package com.loopers.infrastructure.feign.commerce;

import java.util.List;

public class CommerceApiDto {

    public record UserResponse(Long id, String userId, String email) {}

    public record ProductBulkRequest(List<Long> productIds) {}

    public record ProductResponse(Long id, String name, Long price, String imageUrl, String brandName) {}
}
