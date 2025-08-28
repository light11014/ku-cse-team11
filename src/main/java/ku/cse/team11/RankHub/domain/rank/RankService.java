package ku.cse.team11.RankHub.domain.rank;

import ku.cse.team11.RankHub.domain.content.ContentRepository;
import ku.cse.team11.RankHub.domain.content.ContentType;
import ku.cse.team11.RankHub.domain.content.Platform;
import ku.cse.team11.RankHub.domain.tier.Tier;
import ku.cse.team11.RankHub.domain.tier.TierStats;
import ku.cse.team11.RankHub.domain.tier.TierStatsRepository;
import ku.cse.team11.RankHub.domain.tier.TierStatsService;
import ku.cse.team11.RankHub.dto.auth.RankResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankService {
    private final RankRepository rankRepository;
    private final TierStatsRepository tierStatsRepository;

    // Rank 최신화
    @Transactional
    public void refreshRanks() {
        rankRepository.refreshViewRanks(); // 조회수 랭크 최신화
    }

    @Transactional
    public List<RankResponse> getRanks(ContentType type, boolean overall, Platform platform, Integer limit) {
        List<Rank> ranks;
        Pageable pageable = (limit != null) ? PageRequest.of(0, limit) : Pageable.unpaged();

        if (overall) {
            ranks = rankRepository.findByContentType(type, pageable);
        } else {
            ranks = rankRepository.findByContentTypeAndPlatform(type, platform, pageable);
        }

        if (ranks.isEmpty()) {
            return List.of();
        }

        List<Long> contentIds = ranks.stream()
                .map(r -> r.getContent().getId())
                .toList();

        Map<Long, Tier> avgTierMap = tierStatsRepository.findByContentIdIn(contentIds).stream()
                .collect(Collectors.toMap(
                        TierStats::getContentId,
                        TierStats::getAvgTier
                ));

        return ranks.stream()
                .map(r -> RankResponse.from(
                        r,
                        avgTierMap.getOrDefault(r.getContent().getId(), Tier.None),
                        overall
                ))
                .toList();
    }
}