package ku.cse.team11.RankHub.dto.auth;

import ku.cse.team11.RankHub.domain.content.Content;
import ku.cse.team11.RankHub.domain.tier.Tier;

public record ContentDto(
        Long id,
        String title,
        String authors,
        String thumbnailUrl,
        String platform,
        Long views,
        Long likes,
        String language,
        Tier tier
) {
    public static ContentDto from(Content content, Tier tier) {
        return new ContentDto(
                content.getId(),
                content.getTitle(),
                content.getAuthors(),
                content.getThumbnailUrl(),
                content.getPlatform().name(),
                content.getViews(),
                content.getLikes(),
                content.getLanguage().name(),
                tier
        );
    }
}
