package ku.cse.team11.RankHub.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Content {
    @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 기본 정보
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String authors;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType contentType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    private String thumbnailUrl;

    @Column(nullable = false)
    private String contentUrl;

    private Integer totalEpisodes;

    // 초기만 문자열
    @Column(columnDefinition = "TEXT")
    private String tags;

    private String category;

    private String ageRating;

    private String pubPeriod;

    // 통계
    private Long views = 0L;

    private Double rating = 0.0;

    private Long likes = 0L;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
