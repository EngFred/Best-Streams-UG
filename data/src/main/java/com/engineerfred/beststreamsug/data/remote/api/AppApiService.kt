package com.engineerfred.beststreamsug.data.remote.api

import com.engineerfred.beststreamsug.data.remote.dto.ApiResponseDto
import com.engineerfred.beststreamsug.data.remote.dto.BannerRequest
import com.engineerfred.beststreamsug.data.remote.dto.CategoryDto
import com.engineerfred.beststreamsug.data.remote.dto.ContentDto
import com.engineerfred.beststreamsug.data.remote.dto.ContentTypeDto
import com.engineerfred.beststreamsug.data.remote.dto.LanguageDto
import com.engineerfred.beststreamsug.data.remote.dto.ContentBrowseRequest
import com.engineerfred.beststreamsug.data.remote.dto.ContentDetailRequest
import com.engineerfred.beststreamsug.data.remote.dto.ContentDetailsDto
import com.engineerfred.beststreamsug.data.remote.dto.EpisodeRequest
import com.engineerfred.beststreamsug.data.remote.dto.EpisodeDto
import com.engineerfred.beststreamsug.data.remote.dto.RelatedContentRequest
import com.engineerfred.beststreamsug.data.remote.dto.SearchContentRequest
import com.engineerfred.beststreamsug.data.remote.dto.SectionDto
import com.engineerfred.beststreamsug.data.remote.dto.SectionRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AppApiService {
    @POST("get_category")
    suspend fun getCategories(
        @Body request: Map<String, @JvmSuppressWildcards Any?>,
    ): Response<ApiResponseDto<List<CategoryDto>>>

    @POST("get_type")
    suspend fun getTypes(
        @Body request: Map<String, @JvmSuppressWildcards Any?>,
    ): Response<ApiResponseDto<List<ContentTypeDto>>>

    @POST("get_language")
    suspend fun getLanguages(
        @Body request: Map<String, @JvmSuppressWildcards Any?>,
    ): Response<ApiResponseDto<List<LanguageDto>>>

    @POST("get_banner")
    suspend fun getBanners(
        @Body request: BannerRequest,
    ): Response<ApiResponseDto<List<ContentDto>>>

    @POST("section_list")
    suspend fun getSections(
        @Body request: SectionRequest,
    ): Response<ApiResponseDto<List<SectionDto>>>

    @POST("content_by_category")
    suspend fun getContentByCategory(
        @Body request: ContentBrowseRequest,
    ): Response<ApiResponseDto<List<ContentDto>>>

    @POST("content_by_language")
    suspend fun getContentByLanguage(
        @Body request: ContentBrowseRequest,
    ): Response<ApiResponseDto<List<ContentDto>>>

    @POST("content_detail")
    suspend fun getContentDetails(
        @Body request: ContentDetailRequest,
    ): Response<ApiResponseDto<List<ContentDetailsDto>>>

    @POST("get_video_by_season_id")
    suspend fun getEpisodes(
        @Body request: EpisodeRequest,
    ): Response<ApiResponseDto<List<EpisodeDto>>>

    @POST("get_releted_content")
    suspend fun getRelatedContent(
        @Body request: RelatedContentRequest,
    ): Response<ApiResponseDto<List<ContentDto>>>

    @POST("search_content")
    suspend fun searchContent(
        @Body request: SearchContentRequest,
    ): Response<ApiResponseDto<List<ContentDto>>>
}
