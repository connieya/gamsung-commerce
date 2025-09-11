package com.loopers.domain.rank;

import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductInfo> getProductRanking(LocalDate date, int page, int size) {
        List<Long> rankedProductIds = rankingRepository.getRankingInfo(date, page, size)
                .stream()
                .map(Long::parseLong)
                .toList();
        List<ProductInfo> productInfos = productRepository.findRankByIds(rankedProductIds);

        Map<Long, ProductInfo> productMap = productInfos.stream()
                .collect(Collectors.toMap(ProductInfo::getProductId, productInfo -> productInfo));

        return rankedProductIds.stream()
                .map(productMap::get)
                .collect(Collectors.toList());

    }

}
