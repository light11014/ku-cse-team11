package ku.cse.team11.RankHub.domain.tier;

import ku.cse.team11.RankHub.domain.content.Content;
import ku.cse.team11.RankHub.domain.content.ContentRepository;
import ku.cse.team11.RankHub.dto.auth.ContentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TierStatsService {
    private final TierStatsRepository tierStatsRepository;
    private final TierListRepository tierListRepository;

    @Transactional
    public TierStats saveOrUpdateStats(Long contentId) {
        // 개별 평가 기준으로 평균/카운트 계산
        Double avgScore = tierListRepository.getAverageScoreByContentId(contentId);
        Integer ratingCount = tierListRepository.countByContentId(contentId);

        // 최종 티어 변환
        Tier finalTier = (ratingCount == 0) ? Tier.None : convertScoreToTier(avgScore);

        // 기존 TierStats 있으면 업데이트, 없으면 새로 생성
        TierStats stats = tierStatsRepository.findByContentId(contentId)
                .map(existing -> {
                    existing.updateStats(finalTier, avgScore, ratingCount);
                    return existing;
                })
                .orElseGet(() -> tierStatsRepository.save(
                        TierStats.builder()
                                .contentId(contentId)
                                .avgScore(avgScore == null ? 0.0 : avgScore)
                                .avgTier(finalTier)
                                .ratingCount(ratingCount)
                                .build()
                ));

        return stats;
    }

    private Tier convertScoreToTier(Double avg) {
        if (avg >= 37) return Tier.S;
        else if (avg >= 31) return Tier.A;
        else if (avg >= 26) return Tier.B;
        else if (avg >= 20) return Tier.C;
        else if (avg >= 15) return Tier.D;
        else return Tier.F;
    }

    @Transactional(readOnly = true)
    public Tier getAvgTierByContentId(Long contentId) {
        return tierStatsRepository.findByContentId(contentId)
                .map(TierStats::getAvgTier)
                .orElse(Tier.None); // 평가 없으면 None
    }
}
