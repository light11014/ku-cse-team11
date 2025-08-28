package ku.cse.team11.RankHub.domain.comment;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_comment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 댓글 고유 ID

    @Column(name = "content_id", nullable = false)
    private Long contentId;  // 대상 컨텐츠 ID

    @Column(name = "member_id", nullable = false)
    private Long memberId;  // 작성자 ID

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;  // 작성 시간

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;  // 댓글 내용

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
