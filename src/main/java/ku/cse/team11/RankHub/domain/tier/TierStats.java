package ku.cse.team11.RankHub.domain.tier;

import jakarta.persistence.*;
import ku.cse.team11.RankHub.domain.content.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class TierStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    private Integer ratingCount;

    private Double avgScore;

    @Enumerated(EnumType.STRING)
    private Tier avgTier;

    public void updateStats(Tier finalTier, Double avgScore, Integer ratingCount) {
        this.avgTier = finalTier;
        this.avgScore = avgScore;
        this.ratingCount = ratingCount;
    }
}
