package ku.cse.team11.RankHub.controller;


import com.fasterxml.jackson.databind.node.ObjectNode;
import ku.cse.team11.RankHub.domain.content.Content;
import ku.cse.team11.RankHub.domain.content.ContentType;
import ku.cse.team11.RankHub.domain.content.Platform;
import ku.cse.team11.RankHub.domain.search.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private Map<String, String> errorBody(String message) {
        return Map.of("error", message);
    }

    @GetMapping()
    public ResponseEntity<?> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ContentType contentType,
            @RequestParam(required = false) Platform platform,
            @RequestParam(required = false) Integer minEpisode,
            @RequestParam(required = false) Integer maxEpisode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String lang

    ) {
        try{
            Page<ObjectNode> results = searchService.search(
                    keyword, contentType, platform, minEpisode, maxEpisode, page, size,lang
            );
            return ResponseEntity.ok(results);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody(e.getMessage()));
        }

    }
}
