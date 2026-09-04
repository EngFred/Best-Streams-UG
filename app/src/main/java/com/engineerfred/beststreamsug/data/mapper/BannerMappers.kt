package com.engineerfred.beststreamsug.data.mapper

import com.engineerfred.beststreamsug.data.remote.dto.ContentDto
import com.engineerfred.beststreamsug.domain.model.Banner

fun ContentDto.toBanner(vjName: String? = null): Banner? {
    val content = toDomain(vjName) ?: return null
    return Banner(
        content = content,
        categoryNames = categoryName.orEmpty()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() },
    )
}
