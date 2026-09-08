package com.engineerfred.beststreamsug.domain.repository

import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.domain.model.ContentSection

interface SectionRepository {
    suspend fun getSections(isHomeScreen: Int, typeId: Int): AppResult<List<ContentSection>>
}
