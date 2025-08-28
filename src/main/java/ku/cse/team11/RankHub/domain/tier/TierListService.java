package ku.cse.team11.RankHub.domain.tier;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TierListService {
    private final TierListRepository tierListRepository;
    private final TierStatsService tierStatsService;

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
                        .score(tier.getScore())
                        .build()));

        tierStatsService.saveOrUpdateStats(contentId);

        return tierList;
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
        tierStatsService.saveOrUpdateStats(contentId);
    }
}
