package com.engineerfred.beststreamsug.domain.usecase

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.Category
import com.engineerfred.beststreamsug.domain.model.ContentType
import com.engineerfred.beststreamsug.domain.model.Language
import com.engineerfred.beststreamsug.domain.repository.MetadataRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class MetadataUseCasesTest {
    private val repository = object : MetadataRepository {
        override suspend fun getCategories() = AppResult.Success(listOf(Category(1, "Action", null, 0)))
        override suspend fun getContentTypes() = AppResult.Success(listOf(ContentType(1, "Movies", 1, null, 0)))
        override suspend fun getLanguages() = AppResult.Success(listOf(Language(36, "Luganda", null, 0)))
    }

    @Test
    fun getCategoriesDelegatesToRepository() = runBlocking {
        assertEquals("Action", GetCategoriesUseCase(repository)().let { (it as AppResult.Success).data.single().name })
    }

    @Test
    fun getContentTypesDelegatesToRepository() = runBlocking {
        assertEquals("Movies", GetContentTypesUseCase(repository)().let { (it as AppResult.Success).data.single().name })
    }

    @Test
    fun getLanguagesDelegatesToRepository() = runBlocking {
        assertEquals("Luganda", GetLanguagesUseCase(repository)().let { (it as AppResult.Success).data.single().name })
    }
}
