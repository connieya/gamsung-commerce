package com.loopers.domain.common;

public record PageInfo(
        int currentPage,
        int pageSize,
        int totalPages,
        long totalElements,
        boolean hasNext) {

    public static PageInfo create(int currentPage, int pageSize, int totalPages, long totalElements, boolean hasNext) {
        return new PageInfo(currentPage, pageSize, totalPages, totalElements, hasNext);
    }

}
