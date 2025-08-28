package ku.cse.team11.RankHub.controller;

import ku.cse.team11.RankHub.domain.content.ContentType;
import ku.cse.team11.RankHub.domain.content.Platform;
import ku.cse.team11.RankHub.domain.rank.RankService;
import ku.cse.team11.RankHub.domain.translation.TranslateService;
import ku.cse.team11.RankHub.dto.auth.RankResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ranks")
public class RankController {

    private final RankService rankService;
    private final TranslateService translateService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshRanks() {
        rankService.refreshRanks();
        return ResponseEntity.ok(Map.of("message", "Ranks refreshed successfully"));
    }

    @GetMapping("/{type}/all")
    public ResponseEntity<List<RankResponse>> getOverallRanks(
            @PathVariable("type") ContentType type,
            @RequestParam(required = false) Integer limit,
            @RequestParam(name = "lang", required = false) String lang)
    {
        List<RankResponse> rows = rankService.getRanks(type, true, null, limit);
        if (lang == null || lang.isBlank()) {
            return ResponseEntity.ok(rows);
        }
        return ResponseEntity.ok(translateService.translateRankResponses(rows, lang));
    }

    @GetMapping("/{type}/platform/{platform}")
    public ResponseEntity<List<RankResponse>> getPlatformRanks(
            @PathVariable("type") ContentType type,
            @PathVariable("platform") Platform platform,
            @RequestParam(required = false) Integer limit,
            @RequestParam(name = "lang", required = false) String lang
    ) {

        List<RankResponse> rows = rankService.getRanks(type, false, platform, limit);
        if (lang == null || lang.isBlank()) {
            return ResponseEntity.ok(rows);
        }
        return ResponseEntity.ok(translateService.translateRankResponses(rows, lang));

    }
}
