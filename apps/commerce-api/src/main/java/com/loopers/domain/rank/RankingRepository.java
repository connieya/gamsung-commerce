package com.loopers.domain.rank;

import java.time.LocalDate;
import java.util.Set;

public interface RankingRepository {

    Set<String> getRankingInfo(LocalDate date, int page, int size);
}
