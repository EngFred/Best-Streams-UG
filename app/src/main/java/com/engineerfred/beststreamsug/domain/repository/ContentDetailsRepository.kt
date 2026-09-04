package com.engineerfred.beststreamsug.domain.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.ContentDetails

interface ContentDetailsRepository {
    suspend fun getContentDetails(
        contentId: Int,
        typeId: Int,
        videoType: Int,
    ): AppResult<ContentDetails>
}
