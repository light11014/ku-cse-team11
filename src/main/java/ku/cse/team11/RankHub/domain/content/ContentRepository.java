package ku.cse.team11.RankHub.domain.content;

import ku.cse.team11.RankHub.dto.auth.RankResponse;
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
            (:keyword IS NULL OR c.title ILIKE CONCAT('%', CAST(:keyword AS string), '%'))
        AND
            (:contentType IS NULL OR c.contentType = :contentType)

          AND
            (:platform IS NULL OR c.platform = :platform)

          AND
            (
              (:minEpisode IS NULL AND :maxEpisode IS NULL)
              OR
              (
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

}
