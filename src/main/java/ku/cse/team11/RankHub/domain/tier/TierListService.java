package ku.cse.team11.RankHub.domain.tier;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TierService {
    private final TierListRepository tierListRepository;
    private final TierStatsRepository tierStatsRepository;

    @Transactional
    public TierList saveOrUpdateTier(Long memberId, Long contentId, Tier tier) {
        // 개인 티어 등록/수정
        TierList tierList = tierListRepository.findByMemberIdAndContentId(memberId, contentId)
                .map(existing -> {
                    // 이미 있으면 업데이트
                    existing.updateTier(tier);
                    return existing;
                })
                .orElseGet(() -> tierListRepository.save(TierList.builder()
                            .memberId(memberId)
                            .contentId(contentId)
                            .tier(tier)
                            .build()));

        updateContentTier(contentId, tier);

        return tierList;
    }

    private void updateContentTier(Long contentId, Tier tier) {
        // 평균 점수
        Double avgScore = tierListRepository.getAverageScoreByContentId(contentId);
        Integer ratingCount = tierListRepository.countByContentId(contentId);

        // 평균 점수 → 최종 티어 변환
        Tier finalTier = convertScoreToTier(avgScore);

        TierStats stats = tierStatsRepository.findByContentId(contentId)
                .map(existing -> {
                    // 이미 있으면 업데이트
                    existing.updateTier(finalTier, avgScore, ratingCount);
                    return existing;
                })
                .orElseGet(() -> TierStats.builder()
                        .contentId(contentId)
                        .avgTier(finalTier)
                        .avgScore(avgScore)
                        .ratingCount(ratingCount)
                        .build());

        tierStatsRepository.save(stats);
    }

    private Tier convertScoreToTier(Double avg) {
        if (avg == null) return Tier.None; // 평가 없는 경우 기본값
        if (avg >= 4.5) return Tier.S;
        else if (avg >= 3.5) return Tier.A;
        else if (avg >= 2.5) return Tier.B;
        else if (avg >= 1.5) return Tier.C;
        else return Tier.D;
    }

    @Transactional(readOnly = true)
    public List<TierList> getFavoritesByMember(Long memberId) {

        return tierListRepository.findAllByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public long countFavoritesByContent(Long contentId) {

        return tierListRepository.countByContentId(contentId);
    }

    @Transactional
    public void removeTierList(Long memberId, Long contentId) {
        if (!tierListRepository.existsByMemberIdAndContentId(memberId, contentId)) {
            throw new IllegalStateException("등급을 선택하지 않은 content입니다.");
        }
        tierListRepository.deleteByMemberIdAndContentId(memberId, contentId);
    }
}
