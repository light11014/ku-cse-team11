package ku.cse.team11.RankHub.domain.rank;

import ku.cse.team11.RankHub.domain.content.Content;
import ku.cse.team11.RankHub.domain.content.ContentType;
import ku.cse.team11.RankHub.domain.content.Platform;
import ku.cse.team11.RankHub.dto.auth.RankResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RankRepository extends JpaRepository<Rank, Long> {
    Optional<Rank> findByContent(Content content);

    /**
     * 콘텐츠 유형별 랭킹 조회 메서드
     *
     * @param type     조회할 콘텐츠 유형
     * @param pageable 페이징/정렬 정보
     *                 - Pageable.unpaged(): 전체 결과 조회
     *                 - PageRequest.of(0, 100): 상위 100개만 조회
     *
     * @return 지정된 콘텐츠 유형의 Rank 리스트 (Content 포함 fetch join)
     *         viewRank 기준 오름차순 정렬
     */
    @Query("SELECT r FROM Rank r " +
            "JOIN FETCH r.content " +
            "WHERE r.contentType = :type " +
            "ORDER BY r.viewRank ASC")
    List<Rank> findByContentType(@Param("type") ContentType type, Pageable pageable);

    /**
     * 콘텐츠 유형 + 플랫폼별 랭킹 조회 메서드
     *
     * @param type     조회할 콘텐츠 유형
     * @param platform 조회할 플랫폼 유형
     * @param pageable 페이징/정렬 정보
     *                 - Pageable.unpaged(): 전체 결과 조회
     *                 - PageRequest.of(0, 100): 상위 100개만 조회
     *
     * @return 지정된 콘텐츠 유형의 Rank 리스트 (Content 포함 fetch join)
     *         viewRank 기준 오름차순 정렬
     */
    @Query("SELECT r FROM Rank r " +
            "JOIN FETCH r.content " +
            "WHERE r.contentType = :type " +
            "AND r.platform = :platform " +
            "ORDER BY r.platformViewRank ASC")
    List<Rank> findByContentTypeAndPlatform(
            @Param("type") ContentType type, @Param("platform") Platform platform, Pageable pageable);

    @Modifying
    @Query(value = """
        INSERT INTO rank (content_id, content_type, platform, views, view_rank, platform_view_rank)
        SELECT id,
               content_type,
               platform,
               CASE
                   WHEN platform = 'NAVER_WEBTOON' THEN likes * 150
                   ELSE views
               END AS calc_views,
               RANK() OVER (PARTITION BY content_type ORDER BY
                            CASE WHEN platform = 'NAVER_WEBTOON' THEN likes * 10 ELSE views END DESC),
               RANK() OVER (PARTITION BY content_type, platform ORDER BY
                            CASE WHEN platform = 'NAVER_WEBTOON' THEN likes * 10 ELSE views END DESC)
        FROM content
        ON CONFLICT (content_id) DO UPDATE
        SET views = EXCLUDED.views,
            view_rank = EXCLUDED.view_rank,
            platform_view_rank = EXCLUDED.platform_view_rank,
            updated_at = now();
            """, nativeQuery = true)
    void refreshViewRanks();
}
