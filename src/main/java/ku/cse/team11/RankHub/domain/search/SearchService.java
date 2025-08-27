package ku.cse.team11.RankHub.domain.search;

import com.fasterxml.jackson.databind.node.ObjectNode;
import ku.cse.team11.RankHub.domain.content.Content;
import ku.cse.team11.RankHub.domain.content.ContentRepository;
import ku.cse.team11.RankHub.domain.content.ContentType;
import ku.cse.team11.RankHub.domain.content.Platform;
import ku.cse.team11.RankHub.domain.translation.TranslateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class SearchService{

    private final ContentRepository contentRepository;
    private final TranslateService translateService;

    @Transactional(readOnly = true)
    public Page<ObjectNode> search(
            String keyword,
            ContentType contentType,
            Platform platform,
            Integer minEpisode,
            Integer maxEpisode,
            int page,
            int size,
            String targetLang
    ) {
        String kw = (keyword == null) ? null : keyword.trim();
        if (kw != null && !kw.isEmpty() && kw.length() < 2) {
            throw new IllegalArgumentException("검색어는 최소 2글자 이상이어야 합니다.");
        }

        // 에피소드 범위 sanity check (선택)
        if (minEpisode != null && minEpisode < 0) minEpisode = 0;
        if (maxEpisode != null && maxEpisode < 0) maxEpisode = 0;
        if (minEpisode != null && maxEpisode != null && minEpisode > maxEpisode) {
            // 필요 시 swap 하거나 예외 처리
            int tmp = minEpisode;
            minEpisode = maxEpisode;
            maxEpisode = tmp;
        }
        Page<Content> search = contentRepository.search(
                kw,
                contentType,
                platform,
                minEpisode,
                maxEpisode,
                PageRequest.of(page, size));
        return translateService.translateSearchPage(search, targetLang);
    }
}