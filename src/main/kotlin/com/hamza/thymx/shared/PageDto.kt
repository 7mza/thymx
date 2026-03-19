package com.hamza.thymx.shared

import org.springframework.data.domain.Page
import org.springframework.data.domain.Sort

data class PageDto<T>(
    val content: List<T>,
    val page: PageMeta,
    val sort: List<SortField>,
)

data class PageMeta(
    val isFirst: Boolean,
    val isLast: Boolean,
    val number: Int,
    val size: Int,
    val totalPages: Int,
    val totalElements: Long,
)

data class SortField(
    val property: String,
    val direction: Sort.Direction,
)

fun <T : Any> Page<T>.toDto(): PageDto<T> =
    PageDto(
        content = this.content,
        page =
            PageMeta(
                size = this.size,
                number = this.number,
                totalElements = this.totalElements,
                totalPages = this.totalPages,
                isFirst = this.isFirst,
                isLast = this.isLast,
            ),
        sort = this.sort.toList().map { SortField(it.property, it.direction) },
    )
