package ku.cse.team11.RankHub.controller;

import ku.cse.team11.RankHub.domain.content.Content;
import ku.cse.team11.RankHub.domain.content.ContentRepository;
import ku.cse.team11.RankHub.domain.translation.TranslateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/content")
public class ContentController {

    private final ContentRepository contentRepository;
    private final TranslateService translateService; // Google Cloud Translation v3 사용

    @GetMapping("/{contentId}")
    public Content getContentById(
            @PathVariable Long contentId,
            @RequestParam(name = "lang", required = false) String targetLang
    ) {
        Content c = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Content not found"));

        // 1) 현재 컨텐츠의 원문 언어(sourceLang) 결정
        //    - 지금은 lang 컬럼이 없으므로 기본 "ko"
        //    - 나중에 getLang()이 생기면 자동으로 사용됨(리플렉션)
        String sourceLang = "ko";
        try {

            Object val = c.getLanguage();
            if (val != null && !val.toString().isBlank()) {
                sourceLang = normalizeLang(val.toString());
            }
        } catch (Exception e) {
            // 예외시에도 안전하게 기본 "ko"
        }

        // 2) 요청 언어 보정
        String outLang = normalizeLang(targetLang);

        // 3) 번역이 필요 없는 경우(요청 없음 또는 동일 언어) → 원문 그대로
        if (outLang == null || outLang.equalsIgnoreCase(sourceLang)) {
//            return ResponseEntity.ok(responseBody(c.getId(), c.getTitle(), c.getDescription(), sourceLang));
            return c;
        }

        // 4) 번역 필요 → title/description만 번역

        c.setTitle(translateSafe(c.getTitle(), outLang));
        c.setDescription(translateSafe(c.getDescription(), outLang));
//        return ResponseEntity.ok(responseBody(c.getId(), outTitle, outDesc, outLang));
        return c;
    }

    // ---- helpers ----

    private Map<String, Object> responseBody(Long id, String title, String description, String lang) {
        // 필요한 필드만 노출. 다른 필드가 필요하면 여기에 추가하세요.
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", id);
        body.put("title", title);
        body.put("description", description);
        body.put("language", lang); // 응답의 실제 언어
        return body;
    }

    private String translateSafe(String text, String targetLang) {
        if (text == null || text.isBlank()) return text;
        try {
            return translateService.translate(text, targetLang); // v3 클라이언트 사용
        } catch (Exception e) {
            // 번역 실패 시 원문 반환(서비스 연속성 보장)
            return text;
        }
    }

    private String normalizeLang(String lang) {
        if (lang == null) return null;
        String v = lang.trim();
        if (v.isEmpty()) return null;
        if (v.equalsIgnoreCase("kr")) return "ko";
        return v.toLowerCase(); // "en", "ko", "ja", "zh-cn" 등
    }
}