package ku.cse.team11.RankHub.dto.auth;

import ku.cse.team11.RankHub.domain.content.Content;
import ku.cse.team11.RankHub.domain.rank.Rank;

public record ContentDto(
        Long id,
        String title,
        String authors,
        String thumbnailUrl,
        String platform,
        Long views,
        Long likes
) {
    public static ContentDto from(Content content) {
        return new ContentDto(
                content.getId(),
                content.getTitle(),
                content.getAuthors(),
                content.getThumbnailUrl(),
                content.getPlatform().name(),
                content.getViews(),
                content.getLikes()
        );
    }
}
