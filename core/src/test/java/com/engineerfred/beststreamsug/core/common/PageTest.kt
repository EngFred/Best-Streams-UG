package com.engineerfred.beststreamsug.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class PageTest {
    @Test
    fun representsApiPaginationWithoutApiFieldNames() {
        val page = Page(
            items = listOf("first"),
            currentPage = 1,
            totalPages = 3,
            totalItems = 121,
            hasMore = true,
        )

        assertEquals(1, page.currentPage)
        assertEquals(3, page.totalPages)
        assertEquals(121, page.totalItems)
        assertEquals(true, page.hasMore)
    }
}
