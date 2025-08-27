package ku.cse.team11.RankHub.controller;

import ku.cse.team11.RankHub.domain.content.Content;
import ku.cse.team11.RankHub.domain.content.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/content")
public class ContentController {
    private final ContentRepository contentRepository;

    @GetMapping("/{contentId}")
    public Content getContentById(@PathVariable Long contentId) {
        return contentRepository.findById(contentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Content not found"));
    }

}
