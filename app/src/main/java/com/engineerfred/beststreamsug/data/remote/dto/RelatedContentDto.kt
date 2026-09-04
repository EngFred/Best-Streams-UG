package com.engineerfred.beststreamsug.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RelatedContentRequest(
    @SerializedName("video_id")
    val videoId: Int,
    @SerializedName("type_id")
    val typeId: Int,
    @SerializedName("video_type")
    val videoType: Int,
    @SerializedName("page_no")
    val pageNumber: Int,
)
