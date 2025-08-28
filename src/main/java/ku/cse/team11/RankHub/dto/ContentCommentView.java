package ku.cse.team11.RankHub.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ContentCommentView {
    private Long id;
    private Long contentId;
    private Long memberId;
    private LocalDateTime createdAt;
    private String body;
    private String memberName; // Member.name
}
