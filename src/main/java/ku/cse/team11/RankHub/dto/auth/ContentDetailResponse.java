package ku.cse.team11.RankHub.dto.auth;

import ku.cse.team11.RankHub.domain.content.ContentType;
import ku.cse.team11.RankHub.domain.content.Language;
import ku.cse.team11.RankHub.domain.content.Platform;
import ku.cse.team11.RankHub.domain.tier.Tier;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentDetailResponse {
    private Long id;

    // 기본 정보
    private String title;
    private String authors;
    private ContentType contentType;
    private String description;
    private Platform platform;
    private String thumbnailUrl;
    private String contentUrl;
    private Integer totalEpisodes;
    private String tags;
    private String category;
    private String ageRating;
    private String pubPeriod;

    // 통계
    private Long views;
    private Long likes;

    // 언어
    private Language language;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 티어 관련
    private Tier myTier;   // 내가 평가한 티어
    private Tier avgTier;  // 평균 티어
    private Map<String, Object> stats; // { rating_count, rating 분포 }
}