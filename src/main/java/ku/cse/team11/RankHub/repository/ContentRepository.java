package ku.cse.team11.RankHub.repository;

import ku.cse.team11.RankHub.domain.Content;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content, Long> {
}
