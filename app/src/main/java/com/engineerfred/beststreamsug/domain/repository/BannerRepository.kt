package com.engineerfred.beststreamsug.domain.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.Banner

interface BannerRepository {
    suspend fun getBanners(isHomeScreen: String, typeId: Int): AppResult<List<Banner>>
}
