package ku.cse.team11.RankHub.domain.content;

import ku.cse.team11.RankHub.dto.auth.ContentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {
    Optional<Content> findById(Long id);

    @Query("""
    SELECT c FROM Content c
    WHERE
      (:keyword IS NULL OR lower(c.title) LIKE concat('%', lower(:keyword), '%'))
    AND (:contentType IS NULL OR c.contentType = :contentType)
    AND (:platform IS NULL OR c.platform = :platform)
    AND (
      (:minEpisode IS NULL AND :maxEpisode IS NULL)
      OR (
        c.totalEpisodes IS NOT NULL AND c.totalEpisodes > 0
        AND (:minEpisode IS NULL OR c.totalEpisodes >= :minEpisode)
        AND (:maxEpisode IS NULL OR c.totalEpisodes <= :maxEpisode)
      )
    )
""")
    Page<Content> search(
            @Param("keyword") String keyword,
            @Param("contentType") ContentType contentType,
            @Param("platform") Platform platform,
            @Param("minEpisode") Integer minEpisode,
            @Param("maxEpisode") Integer maxEpisode,
            Pageable pageable
    );

    @Query(value = """
        SELECT c.id,
               c.title,
               c.authors,
               c.thumbnail_url AS thumbnailUrl,
               c.platform,
               c.views,
               c.likes,
               c.language,
               ts.avg_tier AS tier
        FROM content c
        JOIN tier_stats ts ON c.id = ts.content_id
        WHERE c.content_type = :contentType
        ORDER BY ts.avg_score DESC
    """, nativeQuery = true)
    List<Object[]> findContentsWithTier(@Param("contentType") String contentType);

    @Query(value = """
        SELECT c.id, c.title, c.authors, c.thumbnail_url, c.platform,
               c.views, c.likes, c.language, ts.avg_tier AS tier
        FROM content c
        JOIN tier_stats ts ON c.id = ts.content_id
        WHERE c.content_type = :contentType
          AND c.platform = :platform
        ORDER BY ts.avg_score DESC
    """, nativeQuery = true)
    List<Object[]> findContentsWithTierAndPlatform(@Param("contentType") String contentType,
                                                   @Param("platform") String platform);

}
