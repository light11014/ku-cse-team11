package ku.cse.team11.RankHub.domain.favorite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {


    // 특정 멤버가 즐겨찾기한 콘텐츠 전체 조회
    List<Favorite> findAllByMemberId(Long memberId);

    // 특정 콘텐츠를 즐겨찾기한 멤버 수 조회
    long countByContentId(Long contentId);

    // 특정 멤버가 특정 콘텐츠 즐겨찾기를 삭제
    void deleteByMemberIdAndContentId(Long memberId, Long contentId);

    // 특정 멤버가 특정 콘텐츠를 즐겨찾기했는지 여부 확인
    boolean existsByMemberIdAndContentId(Long memberId, Long contentId);
}
