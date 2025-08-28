package ku.cse.team11.RankHub.controller;

import ku.cse.team11.RankHub.domain.tier.Tier;
import ku.cse.team11.RankHub.domain.tier.TierList;
import ku.cse.team11.RankHub.domain.tier.TierListService;
import ku.cse.team11.RankHub.domain.tier.TierStatsService;
import ku.cse.team11.RankHub.dto.auth.ContentDto;
import ku.cse.team11.RankHub.dto.auth.TierRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tier")
@RequiredArgsConstructor
public class TierController {
    private final TierListService tierListService;
    private final TierStatsService tierStatsService;
    private Map<String, String> errorBody(String message) {
        return Map.of("error", message);
    }

    @PostMapping
    public ResponseEntity<?> saveOrUpdateTier(@RequestBody TierRequest request) {
        try {
            TierList tierList = tierListService.saveOrUpdateTier(
                    request.memberId(),
                    request.contentId(),
                    request.tier()
            );
            return ResponseEntity.ok(tierList);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errorBody(e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> removeTier(
            @RequestParam Long memberId,
            @RequestParam Long contentId
    ) {
        try{
            tierListService.removeTierList(memberId, contentId);
            return ResponseEntity.ok("티어를 취쇠했습니다.");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody(e.getMessage()));
        }
    }
}
