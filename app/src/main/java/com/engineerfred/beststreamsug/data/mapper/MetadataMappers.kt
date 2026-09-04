package com.engineerfred.beststreamsug.data.mapper

import com.engineerfred.beststreamsug.data.remote.dto.CategoryDto
import com.engineerfred.beststreamsug.data.remote.dto.ContentTypeDto
import com.engineerfred.beststreamsug.data.remote.dto.LanguageDto
import com.engineerfred.beststreamsug.domain.model.Category
import com.engineerfred.beststreamsug.domain.model.ContentType
import com.engineerfred.beststreamsug.domain.model.Language

fun CategoryDto.toDomain(): Category? {
    val categoryId = id ?: return null
    val categoryName = name?.takeIf { it.isNotBlank() } ?: return null
    return Category(
        id = categoryId,
        name = categoryName,
        imageUrl = image?.takeIf { it.isNotBlank() },
        sortOrder = sortOrder ?: 0,
    )
}

fun ContentTypeDto.toDomain(): ContentType? {
    val contentTypeId = id ?: return null
    val contentTypeName = name?.takeIf { it.isNotBlank() } ?: return null
    val contentType = type ?: return null
    return ContentType(
        id = contentTypeId,
        name = contentTypeName,
        type = contentType,
        iconUrl = icon?.takeIf { it.isNotBlank() },
        sortOrder = sortOrder ?: 0,
    )
}

fun LanguageDto.toDomain(): Language? {
    val languageId = id ?: return null
    val languageName = name?.takeIf { it.isNotBlank() } ?: return null
    return Language(
        id = languageId,
        name = languageName,
        imageUrl = image?.takeIf { it.isNotBlank() },
        sortOrder = sortOrder ?: 0,
    )
}
