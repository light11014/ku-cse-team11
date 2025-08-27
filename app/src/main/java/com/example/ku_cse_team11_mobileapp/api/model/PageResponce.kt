package com.example.ku_cse_team11_mobileapp.api.model

data class PageResponse<T>(
    val content: List<T>,          // ← 중요!
    val totalPages: Int,
    val totalElements: Long,
    val size: Int,
    val number: Int,               // 0부터 시작
    val numberOfElements: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean,
    val sort: SortInfo?
)

data class SortInfo(
    val sorted: Boolean,
    val unsorted: Boolean,
    val empty: Boolean
)