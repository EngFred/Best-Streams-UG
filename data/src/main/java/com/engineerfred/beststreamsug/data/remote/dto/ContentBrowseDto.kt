package com.engineerfred.beststreamsug.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ContentBrowseRequest(
    @SerializedName("category_id")
    val categoryId: Int? = null,
    @SerializedName("language_id")
    val languageId: Int? = null,
    @SerializedName("page_no")
    val pageNumber: Int,
    @SerializedName("order_by_upload")
    val orderByUpload: String? = null,
    @SerializedName("order_by_view")
    val orderByView: String? = null,
    @SerializedName("order_by_like")
    val orderByLike: String? = null,
)

data class ContentDto(
    val id: Int? = null,
    @SerializedName("type_id")
    val typeId: Int? = null,
    @SerializedName("video_type")
    val videoType: Int? = null,
    val name: String? = null,
    val thumbnail: String? = null,
    val image: String? = null,
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
    @SerializedName("created_at")
    val createdAt: String? = null,
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
    @SerializedName("language_id")
    val languageId: String? = null,
    @SerializedName("language_name")
    val languageName: String? = null,
)
