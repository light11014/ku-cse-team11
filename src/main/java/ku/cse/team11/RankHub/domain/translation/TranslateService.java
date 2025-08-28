package ku.cse.team11.RankHub.domain.translation;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.translate.v3.*;
import com.google.cloud.translate.v3.LocationName;
import ku.cse.team11.RankHub.domain.content.Content;
import ku.cse.team11.RankHub.dto.auth.ContentDto;
import ku.cse.team11.RankHub.dto.auth.RankResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TranslateService {

    private final TranslationServiceClient client;
    private final ObjectMapper objectMapper; // 스프링이 기본 주입

    @Value("${app.google.project-id}")
    private String projectId;

    @Value("${app.google.location:global}")
    private String location;
    public List<RankResponse> translateRankResponses(List<RankResponse> ranks, String lang) {
        if (ranks == null || ranks.isEmpty()) return ranks;

        final String target = normalizeLang(lang);
        if (target == null || target.isBlank()) return ranks;

        // 원문 언어별로 번역 필요 여부 판정 & 배치 번역을 위한 수집
        // key: sourceLang, value: 번역해야 할 타이틀 목록
        Map<String, List<String>> titlesBySource = new HashMap<>();
        Map<String, List<Integer>> indexesBySource = new HashMap<>();

        for (int i = 0; i < ranks.size(); i++) {
            RankResponse rr = ranks.get(i);
            ContentDto c = rr.content();
            String source = normalizeLangOrDefault(c.language(), "ko");

            if (!target.equalsIgnoreCase(source)) {
                String title = safeText(c.title());
                if (title != null && !title.isBlank()) {
                    titlesBySource.computeIfAbsent(source, k -> new ArrayList<>()).add(title);
                    indexesBySource.computeIfAbsent(source, k -> new ArrayList<>()).add(i);
                }
            }
        }

        if (titlesBySource.isEmpty()) {
            // 모두 동일 언어여서 번역 불필요
            return ranks;
        }

        // source 언어가 섞여 있어도 Google API는 target만 지정하면 되지만,
        // 품질을 위해 source 묶음 단위로 번역(옵션) -> 현재 v3 API는 source 미지정 자동감지
        // 배치 번역 실행
        Map<Integer, String> translatedTitleByIndex = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : titlesBySource.entrySet()) {
            List<String> originals = entry.getValue();
            List<Integer> idxs = indexesBySource.get(entry.getKey());
            List<String> translated = translateBatch(originals, target);
            for (int k = 0; k < idxs.size(); k++) {
                translatedTitleByIndex.put(idxs.get(k), translated.get(k));
            }
        }

        // 새 DTO로 치환
        List<RankResponse> out = new ArrayList<>(ranks.size());
        for (int i = 0; i < ranks.size(); i++) {
            RankResponse rr = ranks.get(i);
            ContentDto c = rr.content();
            String newTitle = translatedTitleByIndex.getOrDefault(i, c.title());

            ContentDto replacedContent = new ContentDto(
                    c.id(),
                    newTitle,
                    c.authors(),
                    c.thumbnailUrl(),
                    c.platform(),
                    c.views(),
                    c.likes(),
                    c.language(),  // 원문 언어는 그대로 유지
                    c.tier()
            );

            out.add(new RankResponse(
                    rr.rank(),
                    rr.score(),
                    replacedContent
            ));
        }
        return out;
    }
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
    private List<String> translateBatch(List<String> texts, String targetLang) {
        if (texts == null || texts.isEmpty()) return Collections.emptyList();

        TranslateTextRequest.Builder builder = TranslateTextRequest.newBuilder()
                .setParent(LocationName.of(projectId, location).toString())
                .setMimeType("text/plain")
                .setTargetLanguageCode(targetLang);

        for (String t : texts) {
            builder.addContents(t == null ? "" : t);
        }

        try {
            TranslateTextResponse res = client.translateText(builder.build());
            if (res.getTranslationsCount() == texts.size()) {
                return res.getTranslationsList()
                        .stream()
                        .map(tr -> tr.getTranslatedText())
                        .collect(Collectors.toList());
            }
        } catch (Exception ignored) {}

        // 실패 시 원문 반환 (길이 보존)
        return new ArrayList<>(texts);
    }

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

    private String normalizeLangOrDefault(String lang, String def) {
        String n = normalizeLang(lang);
        return (n == null) ? def : n;
    }

    private String safeText(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}