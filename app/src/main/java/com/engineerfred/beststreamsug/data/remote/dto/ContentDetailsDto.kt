package com.engineerfred.beststreamsug.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ContentDetailRequest(
    @SerializedName("video_id")
    val videoId: Int,
    @SerializedName("type_id")
    val typeId: Int,
    @SerializedName("video_type")
    val videoType: Int,
)

data class ContentDetailsDto(
    val id: Int? = null,
    @SerializedName("type_id")
    val typeId: Int? = null,
    @SerializedName("video_type")
    val videoType: Int? = null,
    val name: String? = null,
    val thumbnail: String? = null,
    val landscape: String? = null,
    val description: String? = null,
    @SerializedName("video_duration")
    val videoDuration: Long? = null,
    @SerializedName("video_320")
    val video320: String? = null,
    @SerializedName("video_480")
    val video480: String? = null,
    @SerializedName("video_720")
    val video720: String? = null,
    @SerializedName("video_1080")
    val video1080: String? = null,
    @SerializedName("release_date")
    val releaseDate: String? = null,
    @SerializedName("is_premium")
    val isPremium: Int? = null,
    @SerializedName("total_view")
    val totalView: Int? = null,
    @SerializedName("total_like")
    val totalLike: Int? = null,
    @SerializedName("avg_rating")
    val averageRating: Double? = null,
    @SerializedName("total_review")
    val totalReview: Int? = null,
    @SerializedName("category_name")
    val categoryName: String? = null,
    @SerializedName("language_name")
    val languageName: String? = null,
    @SerializedName("trailer_type")
    val trailerType: String? = null,
    @SerializedName("trailer_url")
    val trailerUrl: String? = null,
    @SerializedName("subtitle_type")
    val subtitleType: String? = null,
    @SerializedName("subtitle_lang_1")
    val subtitleLanguage1: String? = null,
    @SerializedName("subtitle_1")
    val subtitle1: String? = null,
    @SerializedName("subtitle_lang_2")
    val subtitleLanguage2: String? = null,
    @SerializedName("subtitle_2")
    val subtitle2: String? = null,
    @SerializedName("subtitle_lang_3")
    val subtitleLanguage3: String? = null,
    @SerializedName("subtitle_3")
    val subtitle3: String? = null,
    @SerializedName("is_user_like")
    val isUserLike: Int? = null,
    @SerializedName("total_comment")
    val totalComment: Int? = null,
    val cast: List<CastDto>? = null,
    val season: List<SeasonDto>? = null,
)

data class CastDto(
    val id: Int? = null,
    val name: String? = null,
    val image: String? = null,
    val type: String? = null,
    @SerializedName("personal_info")
    val personalInfo: String? = null,
)

data class SeasonDto(
    val id: Int? = null,
    val name: String? = null,
    @SerializedName("sort_order")
    val sortOrder: Int? = null,
)
