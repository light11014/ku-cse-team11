package com.example.ku_cse_team11_mobileapp.model.ui

enum class ContentTypeTab(val label: String, val apiParam: String) {
    WEBTOON("웹툰", "WEBTOON"),
    WEBNOVEL("웹소설", "WEBNOVEL"),
    // 필요 시 확장: VIDEO, NOVEL 등 서버 스펙에 맞게
}

enum class PlatformTab(val label: String) {
    ALL("전체"),
    KAKAO_WEBTOON("카카오웹툰"),
    KAKAO_PAGE("카카오페이지"),
    NAVER_WEBTOON("네이버웹툰"),
    // 필요시 확장
}
