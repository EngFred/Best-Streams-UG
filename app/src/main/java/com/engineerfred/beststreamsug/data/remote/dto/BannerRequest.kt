package com.engineerfred.beststreamsug.data.remote.dto

import com.google.gson.annotations.SerializedName

data class BannerRequest(
    @SerializedName("is_home_screen")
    val isHomeScreen: String,
    @SerializedName("type_id")
    val typeId: Int,
)
