package com.engineerfred.beststreamsug.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EpisodeRequest(
    @SerializedName("show_id")
    val showId: Int,
    @SerializedName("season_id")
    val seasonId: Int,
)

data class EpisodeDto(
    val id: Int? = null,
    @SerializedName("show_id")
    val showId: Int? = null,
    @SerializedName("season_id")
    val seasonId: Int? = null,
    val name: String? = null,
    val thumbnail: String? = null,
    val landscape: String? = null,
    val description: String? = null,
    @SerializedName("video_320")
    val video320: String? = null,
    @SerializedName("video_480")
    val video480: String? = null,
    @SerializedName("video_720")
    val video720: String? = null,
    @SerializedName("video_1080")
    val video1080: String? = null,
    @SerializedName("video_duration")
    val videoDuration: Long? = null,
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
    @SerializedName("is_premium")
    val isPremium: Int? = null,
    @SerializedName("total_view")
    val totalView: Int? = null,
    @SerializedName("sort_order")
    val sortOrder: Int? = null,
)
