package ku.cse.team11.RankHub.domain.translation;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.translate.v3.*;
import com.google.cloud.translate.v3.LocationName;
import ku.cse.team11.RankHub.domain.content.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

@Service
@RequiredArgsConstructor
public class TranslateService {

    private final TranslationServiceClient client;
    private final ObjectMapper objectMapper; // 스프링이 기본 주입

    @Value("${app.google.project-id}")
    private String projectId;

    @Value("${app.google.location:global}")
    private String location;

    public String translate(String text, String targetLang) {
        if (text == null || text.isBlank() || targetLang == null || targetLang.isBlank()) return text;

        TranslateTextRequest req = TranslateTextRequest.newBuilder()
                .setParent(LocationName.of(projectId, location).toString())
                .setMimeType("text/plain")
                .setTargetLanguageCode(targetLang)
                .addContents(text)
                .build();

        TranslateTextResponse res = client.translateText(req);
        return res.getTranslationsCount() > 0 ? res.getTranslations(0).getTranslatedText() : text;
    }

    /**
     * search 결과(Page<Content>)의 각 아이템에서 title/description만 target 언어로 번역해 반환.
     * 반환 타입은 Page<JsonNode> 이라서 기존 Page 응답 구조(content, pageable 등)는 그대로 유지됨.
     */
    public Page<ObjectNode> translateSearchPage(Page<Content> page, String lang) {
        final String target = normalizeLang(lang);

        return page.map(content -> {
            // 대상 언어 없거나 원문과 동일하면 그대로 직렬화
            final String source = detectSourceLang(content); // 현재 컬럼 없으므로 "ko" 기본
            if (target == null || target.equalsIgnoreCase(source)) {
                return objectMapper.valueToTree(content);
            }

            // title/description만 번역
            String t = translateSafe(content.getTitle(), target);
            String d = translateSafe(content.getDescription(), target);

            ObjectNode node = objectMapper.valueToTree(content);
            if (t == null) node.putNull("title"); else node.put("title", t);
            if (d == null) node.putNull("description"); else node.put("description", d);
            return node;
        });
    }

    // ---------- helpers ----------

    private String translateSafe(String text, String targetLang) {
        if (text == null || text.isBlank()) return text;
        try {
            return translate(text, targetLang);
        } catch (Exception e) {
            // 실패 시 원문 유지
            return text;
        }
    }

    private String detectSourceLang(Content c) {
        String fallback = "ko"; // 현재 DB는 한글 원문
        try {
            // 나중에 content.lang 컬럼(getLang()) 추가되면 자동 활용
            Object v = c.getLanguage();
            if (v != null) {
                String s = normalizeLang(v.toString());
                if (s != null) return s;
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    private String normalizeLang(String lang) {
        if (lang == null) return null;
        String v = lang.trim();
        if (v.isEmpty()) return null;
        if (v.equalsIgnoreCase("kr")) return "ko"; // 관용 입력 보정
        return v.toLowerCase(); // "en", "ko", "ja", "zh-cn" 등
    }
}