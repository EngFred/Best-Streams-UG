package com.engineerfred.beststreamsug.data.remote.datasource

import com.engineerfred.beststreamsug.core.common.AppError
import com.engineerfred.beststreamsug.core.common.AppResult
import com.engineerfred.beststreamsug.core.network.NetworkErrorMapper
import com.engineerfred.beststreamsug.data.remote.api.AppApiService
import com.engineerfred.beststreamsug.data.remote.dto.SectionDto
import com.engineerfred.beststreamsug.data.remote.dto.SectionRequest
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class SectionRemoteDataSource @Inject constructor(
    private val apiService: AppApiService,
    private val errorMapper: NetworkErrorMapper,
) {
    suspend fun getSections(
        isHomeScreen: Int,
        typeId: Int,
    ): AppResult<List<SectionDto>> = try {
        val response = apiService.getSections(SectionRequest(isHomeScreen, typeId))
        if (!response.isSuccessful) {
            AppResult.Failure(errorMapper.mapHttpStatus(response.code(), response.message()))
        } else {
            val body = response.body()
            when {
                body == null -> AppResult.Failure(AppError.EmptyResponse)
                body.result == null -> AppResult.Failure(AppError.InvalidResponse(body.message))
                else -> AppResult.Success(body.result)
            }
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Throwable) {
        AppResult.Failure(errorMapper.map(exception))
    }
}
