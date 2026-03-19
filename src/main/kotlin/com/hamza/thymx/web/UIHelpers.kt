package com.hamza.thymx.web

import com.hamza.thymx.shared.PageMeta
import com.hamza.thymx.shared.SortField
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.io.Serializable

data class NavbarPopulator(
    val menus: List<NavbarMenu>,
)

data class NavbarMenu(
    val name: String,
    val href: String? = null,
    val activeFlag: String,
    val subMenus: List<NavbarMenu> = emptyList(),
    val authority: String? = null,
)

data class SelectPopulator(
    val selects: List<Select>,
)

data class Select(
    val id: String,
    val text: String,
)

data class RadioPopulator(
    val radios: List<Radio>,
)

data class Radio(
    val value: String,
    val text: String,
)

data class LanguagesPopulator(
    val languages: List<String>,
)

data class PaginationSizesPopulator(
    val sizes: List<Int>,
)

enum class Operation {
    CREATED,
    UPDATED,
    DELETED,
}

// flash = session, will be intercepted by current spring session backend
// https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/flash-attributes.html
data class FlashPopulator(
    val operation: Operation,
    val details: String,
) : Serializable

@Component
class NavigationHelper {
    fun replaceQueryParam(
        name: String,
        value: String,
    ): String =
        ServletUriComponentsBuilder
            .fromCurrentRequest()
            .replaceQueryParam(name, value)
            .toUriString()

    fun replaceQueryParams(params: Map<String, String>): String {
        val builder = ServletUriComponentsBuilder.fromCurrentRequest()
        params.forEach { (name, value) ->
            builder.replaceQueryParam(name, value)
        }
        return builder.toUriString()
    }

    fun isFieldInSort(
        sort: List<SortField>,
        field: String,
    ): Boolean =
        if (sort.isEmpty()) {
            false
        } else {
            sort.any { it.property.equals(field, true) }
        }

    fun getFieldDirectionFromSort(
        sort: List<SortField>,
        field: String,
    ): Boolean = sort.first { it.property.equals(field, true) }.direction == Sort.Direction.ASC

    fun getQueryParam(name: String): List<String> =
        ServletUriComponentsBuilder
            .fromCurrentRequest()
            .query(name)
            .build()
            .queryParams
            .getOrDefault(name, emptyList())
            .filterNotNull()

    fun getQueryParams(): String =
        ServletUriComponentsBuilder
            .fromCurrentRequest()
            .build()
            .query
            ?.let { "?$it" } ?: ""
}

@Component
class PaginationHelper(
    private val nvHelper: NavigationHelper,
    @Value($$"${spring.data.web.pageable.default-page-size}") private val defaultPageSize: Int,
) {
    // window of pages of radius N around the current page bounded by [0, totalPages - 1]
    fun getRange(
        page: PageMeta,
        radius: Int = 3,
    ): IntRange {
        val total = page.totalPages
        if (total == 0) return 0..0
        val current = page.number.coerceIn(0, total - 1)
        var start = (current - radius).coerceAtLeast(0)
        var end = (current + radius).coerceAtMost(total - 1)
        val needed = 2 * radius + 1
        if (end - start + 1 < needed) {
            val shift = needed - (end - start + 1)
            start = (start - shift).coerceAtLeast(0)
            end = (start + needed - 1).coerceAtMost(total - 1)
        }
        return start..end
    }

    fun getPageSummary(page: PageMeta): PageSummary =
        PageSummary(
            currentElements = (page.size.toLong() * (page.number.toLong() + 1)).coerceAtMost(page.totalElements),
            totalElements = page.totalElements,
            currentPage = page.number + 1,
            totalPages = page.totalPages.takeIf { it > 0 } ?: 1,
        )

    fun isPageSizeSelected(size: Int): Boolean {
        val sizeQuery = nvHelper.getQueryParam("size")
        return if (sizeQuery.isEmpty()) {
            size == defaultPageSize
        } else {
            sizeQuery.first() == "$size"
        }
    }
}

data class PageSummary(
    val currentElements: Long,
    val totalElements: Long,
    val currentPage: Int,
    val totalPages: Int,
)
