package ku.cse.team11.RankHub.domain.comment;

import ku.cse.team11.RankHub.dto.ContentCommentView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContentCommentRepository extends JpaRepository<ContentComment, Long> {
    List<ContentComment> findByContentIdOrderByCreatedAtDesc(Long contentId);
    @Query("""
           select new ku.cse.team11.RankHub.dto.ContentCommentView(
               cc.id, cc.contentId, cc.memberId, cc.createdAt, cc.body, m.name
           )
           from ContentComment cc
           left join Member m on m.id = cc.memberId
           where cc.contentId = :contentId
           order by cc.createdAt desc
           """)
    List<ContentCommentView> findViewsByContentId(@Param("contentId") Long contentId);

}