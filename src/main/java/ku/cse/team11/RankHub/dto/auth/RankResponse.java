package ku.cse.team11.RankHub.dto.auth;

import ku.cse.team11.RankHub.domain.rank.Rank;

public record RankResponse(
        Integer rank,
        ContentDto content
) {
    public static RankResponse from(Rank rank, boolean overall) {
        return new RankResponse(
                overall ? rank.getViewRank() : rank.getPlatformViewRank(),
                ContentDto.from(rank.getContent(), rank.getViews())
        );
    }
}
