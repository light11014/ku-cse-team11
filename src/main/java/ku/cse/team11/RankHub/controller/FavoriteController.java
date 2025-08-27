package ku.cse.team11.RankHub.controller;

import ku.cse.team11.RankHub.domain.favorite.Favorite;
import ku.cse.team11.RankHub.domain.favorite.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;
    private Map<String, String> errorBody(String message) {
        return Map.of("error", message);
    }

    @PostMapping
    public ResponseEntity<?> addFavorite(
            @RequestParam Long memberId,
            @RequestParam Long contentId
    ) {
        try{
            Favorite favorite = favoriteService.addFavorite(memberId, contentId);
            return ResponseEntity.ok(favorite);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody(e.getMessage()));
        }
    }

    @GetMapping(params = {"memberId", "contentId"})
    public ResponseEntity<Boolean> isFavorite(
            @RequestParam Long memberId,
            @RequestParam Long contentId
    ) {
        boolean result = favoriteService.isFavorite(memberId, contentId);
        return ResponseEntity.ok(result);
    }

    // 2) 특정 멤버의 모든 즐겨찾기 조회
    //    GET /favorites?memberId={memberId}
    @GetMapping(params = {"memberId"})
    public ResponseEntity<List<Favorite>> getFavoritesByMember(
            @RequestParam Long memberId
    ) {
        List<Favorite> favorites = favoriteService.getFavoritesByMember(memberId);
        return ResponseEntity.ok(favorites);
    }

    // 3) 특정 콘텐츠의 즐겨찾기 수 조회
    //    GET /favorites?contentId={contentId}
    @GetMapping(params = {"contentId"})
    public ResponseEntity<Long> countFavoritesByContent(
            @RequestParam Long contentId
    ) {
        long count = favoriteService.countFavoritesByContent(contentId);
        return ResponseEntity.ok(count);
    }

    @DeleteMapping
    public ResponseEntity<?> removeFavorite(
            @RequestParam Long memberId,
            @RequestParam Long contentId
    ) {
        try{
            favoriteService.removeFavorite(memberId, contentId);
            return ResponseEntity.ok("즐겨찾기가 해제되었습니다.");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody(e.getMessage()));
        }
    }
}
