package ku.cse.team11.RankHub.domain.tier;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TierStatsRepository extends JpaRepository<TierStats, Long> {
    Optional<TierStats> findByContentId(Long contentId);
}
