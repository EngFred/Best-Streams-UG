package com.engineerfred.beststreamsug.data.mapper

import com.engineerfred.beststreamsug.data.remote.dto.ContentDto
import com.engineerfred.beststreamsug.domain.model.ContentKind
import com.engineerfred.beststreamsug.domain.model.ContentSummary
import com.engineerfred.beststreamsug.data.remote.dto.SectionDto
import com.engineerfred.beststreamsug.domain.model.ContentSection

fun String?.resolveVjName(lookup: (Int) -> String?): String? {
    if (isNullOrBlank()) return null
    return split(',')
        .mapNotNull { it.trim().toIntOrNull() }
        .mapNotNull { lookup(it) }
        .firstOrNull()
}

fun ContentDto.toDomain(overriddenVjName: String? = null): ContentSummary? {
    val contentId = id ?: return null
    val contentTitle = name?.takeIf { it.isNotBlank() } ?: return null
    return ContentSummary(
        id = contentId,
        kind = when (typeId ?: videoType) {
            1 -> ContentKind.MOVIE
            2 -> ContentKind.TV_SHOW
            else -> ContentKind.UNKNOWN
        },
        title = contentTitle,
        thumbnailUrl = (thumbnail ?: image)?.takeIf { it.isNotBlank() },
        landscapeUrl = landscape?.takeIf { it.isNotBlank() },
        description = description?.takeIf { it.isNotBlank() },
        durationMillis = videoDuration?.takeIf { it >= 0 },
        defaultVideoUrl = sequenceOf(video320, video480, video720, video1080)
            .firstOrNull { !it.isNullOrBlank() },
        releaseDate = releaseDate?.takeIf { it.isNotBlank() },
        createdAt = createdAt?.takeIf { it.isNotBlank() },
        isPremium = isPremium == 1,
        viewCount = totalView ?: 0,
        likeCount = totalLike ?: 0,
        averageRating = averageRating ?: 0.0,
        reviewCount = totalReview ?: 0,
        vjName = overriddenVjName ?: languageName?.takeIf { it.isNotBlank() },
    )
}

fun SectionDto.toDomain(vjLookup: (Int) -> String? = { null }): ContentSection? {
    val sectionTitle = sequenceOf(sectionTitle, title, sectionName, name)
        .filterNotNull()
        .firstOrNull { it.isNotBlank() }
        ?: return null
    val sectionItems = data.orEmpty().mapNotNull { 
        val resolvedVjName = it.languageId.resolveVjName(vjLookup)
        it.toDomain(resolvedVjName) 
    }
    return ContentSection(
        id = id ?: sectionTitle.hashCode(),
        title = sectionTitle,
        items = sectionItems,
        layout = screenLayout,
    )
}
