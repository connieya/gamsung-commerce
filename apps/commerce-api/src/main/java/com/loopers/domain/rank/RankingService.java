package com.loopers.domain.rank;

import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public RankingInfo getProductRanking(RankingCommand.GetProducts command) {
        List<Long> rankedProductIds = rankingRepository.findProductRanking(command.getDate(), command.getPage(), command.getSize())
                .stream()
                .map(Long::parseLong)
                .toList();
        List<ProductInfo> productInfos = productRepository.findRankByIds(rankedProductIds);

        Map<Long, ProductInfo> productMap = productInfos.stream()
                .collect(Collectors.toMap(ProductInfo::getProductId, productInfo -> productInfo));

        return RankingInfo.from(
                rankedProductIds.stream()
                .map(productMap::get)
                .collect(Collectors.toList())
        );

    }

    @Transactional(readOnly = true)
    public Long getRankOfProduct(RankingCommand.GetProduct command) {
        Long productRank = rankingRepository.findProductRank(command.getDate(), command.getProductId());
        return productRank == null ? null : productRank+ 1;
    }

}
