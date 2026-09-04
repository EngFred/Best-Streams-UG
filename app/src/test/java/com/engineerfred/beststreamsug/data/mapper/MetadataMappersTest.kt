package com.engineerfred.beststreamsug.data.mapper

import com.engineerfred.beststreamsug.data.remote.dto.CategoryDto
import com.engineerfred.beststreamsug.data.remote.dto.ContentTypeDto
import com.engineerfred.beststreamsug.data.remote.dto.LanguageDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MetadataMappersTest {
    @Test
    fun mapsCategoryAndNormalizesOptionalFields() {
        val category = CategoryDto(
            id = 1,
            name = "Action",
            image = "",
            sortOrder = null,
        ).toDomain()

        assertEquals(1, category?.id)
        assertEquals("Action", category?.name)
        assertNull(category?.imageUrl)
        assertEquals(0, category?.sortOrder)
    }

    @Test
    fun rejectsCategoryWithoutRequiredIdentity() {
        assertNull(CategoryDto(id = null, name = "Action").toDomain())
        assertNull(CategoryDto(id = 1, name = " ").toDomain())
    }

    @Test
    fun mapsContentTypeAndLanguage() {
        val contentType = ContentTypeDto(id = 2, name = "TV Shows", type = 2).toDomain()
        val language = LanguageDto(id = 36, name = "Luganda").toDomain()

        assertEquals(2, contentType?.type)
        assertEquals("Luganda", language?.name)
    }
}
