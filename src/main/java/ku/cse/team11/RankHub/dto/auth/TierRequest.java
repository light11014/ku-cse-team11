package ku.cse.team11.RankHub.dto.auth;

import jakarta.validation.constraints.NotBlank;
import ku.cse.team11.RankHub.domain.tier.Tier;

public record TierRequest(
        @NotBlank Long memberId,
        @NotBlank Long contentId,
        @NotBlank Tier tier
) {
}
