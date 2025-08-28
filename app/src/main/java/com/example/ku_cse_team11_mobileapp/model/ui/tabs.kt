package com.example.ku_cse_team11_mobileapp.model.ui

enum class ContentTypeTab(val label: String, val apiParam: String) {
    WEBTOON("웹툰", "WEBTOON"),
    WEBNOVEL("웹소설", "WEBNOVEL"),
    // 필요 시 확장: VIDEO, NOVEL 등 서버 스펙에 맞게
}

enum class PlatformTab(
    val label: String,          // 화면 표시용
    val apiParam: String,       // 서버에 보낼 값
    val supports: Set<String>   // 어떤 ContentType에 노출할지 (apiParam 기준)
) {
    ALL("전체", "ALL", setOf("WEBTOON", "WEBNOVEL")),

    // 웹툰용
    KAKAO_WEBTOON("카카오웹툰", "KAKAO_WEBTOON", setOf("WEBTOON")),
    NAVER_WEBTOON("네이버웹툰", "NAVER_WEBTOON", setOf("WEBTOON")),

    // 공통(웹툰/웹소설 둘 다)
    KAKAO_PAGE("카카오페이지", "KAKAO_PAGE", setOf("WEBTOON", "WEBNOVEL")),

    // 웹소설용
    MUNPIA("문피아", "MUNPIA", setOf("WEBNOVEL")),
    NOVELPIA("노벨피아", "NOVELPIA", setOf("WEBNOVEL"));

    companion object {
        fun tabsFor(contentTypeApi: String): List<PlatformTab> =
            entries.filter { contentTypeApi in it.supports }
    }
}