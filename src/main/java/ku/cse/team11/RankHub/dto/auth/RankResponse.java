package ku.cse.team11.RankHub.dto.auth;

import ku.cse.team11.RankHub.domain.rank.Rank;
import ku.cse.team11.RankHub.domain.tier.Tier;

public record RankResponse(
        Integer rank,
        Long score,
        ContentDto content
) {
    public static RankResponse from(Rank rank, Tier tier, boolean overall) {
        return new RankResponse(
                overall ? rank.getViewRank() : rank.getPlatformViewRank(),
                rank.getViews(),
                ContentDto.from(rank.getContent(), tier)
        );
    }
}
