package com.engineerfred.beststreamsug.domain.model

data class ContentSummary(
    val id: Int,
    val kind: ContentKind,
    val title: String,
    val thumbnailUrl: String?,
    val landscapeUrl: String?,
    val description: String?,
    val durationMillis: Long?,
    val defaultVideoUrl: String?,
    val releaseDate: String?,
    val isPremium: Boolean,
    val viewCount: Int,
    val likeCount: Int,
    val averageRating: Double,
    val reviewCount: Int,
    val createdAt: String? = null,
    val vjName: String? = null,
)
