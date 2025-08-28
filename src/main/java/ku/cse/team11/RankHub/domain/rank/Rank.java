package ku.cse.team11.RankHub.domain.rank;

import jakarta.persistence.*;
import ku.cse.team11.RankHub.domain.content.Content;
import ku.cse.team11.RankHub.domain.content.ContentType;
import ku.cse.team11.RankHub.domain.content.Platform;
import ku.cse.team11.RankHub.domain.tier.Tier;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Rank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false, unique = true)
    private Content content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType contentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    // 조회수 랭크
    private Long views;
    private Integer viewRank;
    private Integer platformViewRank;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Rank(Content content, Long views) {
        this.content = content;
        this.views = views;
        this.contentType = content.getContentType();
        this.platform = content.getPlatform();
    }

    public void updateRanks(Integer overallRank, Integer platformRank) {
        this.viewRank = overallRank;
        this.platformViewRank = platformRank;
    }

    public void updateViews(Long views) {
        this.views = views;
    }
}

