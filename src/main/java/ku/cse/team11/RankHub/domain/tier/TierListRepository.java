package ku.cse.team11.RankHub.domain.tier;

import ku.cse.team11.RankHub.domain.favorite.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface TierListRepository extends JpaRepository<TierList, Long> {

    Optional<TierList> findByMemberIdAndContentId(Long memberId, Long contentId);

    // 특정 멤버가 평가한 콘텐츠 전체 조회
    List<TierList> findAllByMemberId(Long memberId);

    // 특정 콘텐츠를 평가한 멤버 수 조회
    Integer countByContentId(Long contentId);

    // 특정 멤버가 특정 콘텐츠 티어를 삭제
    void deleteByMemberIdAndContentId(Long memberId, Long contentId);

    boolean existsByMemberIdAndContentId(Long memberId, Long contentId);

    @Query("SELECT AVG(t.score) FROM TierList t WHERE t.contentId = :contentId")
    Double getAverageScoreByContentId(@Param("contentId") Long contentId);

    Long countByContentIdAndTier(Long contentId, Tier s);
}
