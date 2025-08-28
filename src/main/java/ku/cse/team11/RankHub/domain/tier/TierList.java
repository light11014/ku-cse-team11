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
@Getter
@Builder
@Table(
        name = "tier_list",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"member_id", "content_id"})
        }
)
public class TierList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false)
    private Tier tier;

    private Integer score;

    public void updateTier(Tier tier) {
        this.tier = tier;
        this.score = tier.getScore();
    }
}
