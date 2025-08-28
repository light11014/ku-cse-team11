package ku.cse.team11.RankHub.domain.content;

import ku.cse.team11.RankHub.domain.tier.Tier;
import ku.cse.team11.RankHub.dto.auth.ContentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class ContentService {
    private final ContentRepository contentRepository;

    public List<ContentDto> getContentsByTier(ContentType contentType) {
        List<Object[]> results = contentRepository.findContentsWithTier(contentType.name());

        return results.stream()
                .map(row -> new ContentDto(
                        ((Number) row[0]).longValue(),          // id
                        (String) row[1],                        // title
                        (String) row[2],                        // authors
                        (String) row[3],                        // thumbnailUrl
                        (String) row[4],                        // platform
                        row[5] != null ? ((Number) row[5]).longValue() : 0L, // views
                        row[6] != null ? ((Number) row[6]).longValue() : 0L, // likes
                        (String) row[7],                        // language
                        Tier.valueOf((String) row[8])           // tier
                ))
                .toList();
    }
}
