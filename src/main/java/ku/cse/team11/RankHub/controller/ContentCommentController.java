package ku.cse.team11.RankHub.controller;

import ku.cse.team11.RankHub.domain.comment.ContentComment;
import ku.cse.team11.RankHub.domain.comment.ContentCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContentCommentController {

    private final ContentCommentRepository commentRepository;

    // [GET] 컨텐츠의 전체 코멘트 조회
    // GET /content/{contentId}/comments
    @GetMapping("/content/{contentId}/comments")
    public ResponseEntity<List<CommentResponse>> listByContentId(@PathVariable Long contentId) {
        List<ContentComment> rows = commentRepository.findByContentIdOrderByCreatedAtDesc(contentId);
        List<CommentResponse> result = rows.stream()
                .map(CommentResponse::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    // [POST] 코멘트 생성 (쿼리 파라미터: userId, body)
    // POST /content/{contentId}/comments?userId=...&body=...
    @PostMapping("/content/{contentId}/comments")
    public ResponseEntity<?> create(
            @PathVariable Long contentId,
            @RequestParam("userId") Long userId,
            @RequestParam("body") String body
    ) {
        if (body == null || body.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "body는 비어 있을 수 없습니다."));
        }
        ContentComment saved = commentRepository.save(
                ContentComment.builder()
                        .contentId(contentId)
                        .memberId(userId)
                        .body(body)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
        return ResponseEntity.created(URI.create("/api/comments/" + saved.getId()))
                .body(CommentResponse.from(saved));
    }

    // [PATCH] 코멘트 본문 수정 (쿼리 파라미터: body)
    // PATCH /comments/{commentId}?body=...
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<?> updateBody(
            @PathVariable Long commentId,
            @RequestParam("body") String body
    ) {
        if (body == null || body.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "body는 비어 있을 수 없습니다."));
        }

        var commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(Map.of("error", "댓글을 찾을 수 없습니다."));
        }

        var comment = commentOpt.get();
        comment.setBody(body);
        ContentComment updated = commentRepository.save(comment);

        return ResponseEntity.ok(CommentResponse.from(updated));
    }



    // [DELETE] 코멘트 삭제
    // DELETE /comments/{commentId}
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> delete(@PathVariable Long commentId) {
        return commentRepository.findById(commentId)
                .map(cc -> {
                    commentRepository.delete(cc);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "댓글을 찾을 수 없습니다.")));
    }

    // ===== 응답 DTO =====
    public record CommentResponse(
            Long id,
            Long contentId,
            Long memberId,
            String body,
            LocalDateTime createdAt
    ) {
        public static CommentResponse from(ContentComment c) {
            return new CommentResponse(c.getId(), c.getContentId(), c.getMemberId(), c.getBody(), c.getCreatedAt());
        }
    }
}
