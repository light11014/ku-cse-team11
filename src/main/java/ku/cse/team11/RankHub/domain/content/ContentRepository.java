package ku.cse.team11.RankHub.domain.content;

import ku.cse.team11.RankHub.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {
    Optional<Content> findById(Long id);
}
