package ku.cse.team11.RankHub.domain.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentCommentRepository extends JpaRepository<ContentComment, Long> {
    List<ContentComment> findByContentIdOrderByCreatedAtDesc(Long contentId);
}