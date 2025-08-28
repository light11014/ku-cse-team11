package ku.cse.team11.RankHub.domain.rank;

import ku.cse.team11.RankHub.domain.content.ContentRepository;
import ku.cse.team11.RankHub.domain.content.ContentType;
import ku.cse.team11.RankHub.domain.content.Platform;
import ku.cse.team11.RankHub.domain.tier.TierStatsService;
import ku.cse.team11.RankHub.dto.auth.RankResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RankService {
    private final RankRepository rankRepository;
    private final TierStatsService tierStatsService;

    // Rank 최신화
    @Transactional
    public void refreshRanks() {
        rankRepository.refreshViewRanks(); // 조회수 랭크 최신화
    }

    @Transactional
    public List<RankResponse> getRanks(ContentType type, boolean overall, Platform platform, Integer limit) {
        List<Rank> ranks;
        if (overall) {
            ranks = rankRepository.findByContentType(
                    type,
                    limit != null ? PageRequest.of(0, limit) : Pageable.unpaged()
            );
        } else {
            ranks = rankRepository.findByContentTypeAndPlatform(
                    type,
                    platform,
                    limit != null ? PageRequest.of(0, limit) : Pageable.unpaged()
            );
        }

        return ranks.stream()
                .map(r -> RankResponse.from(r, tierStatsService.getAvgTierByContentId(r.getContent().getId()), overall))
                .toList();
    }
}