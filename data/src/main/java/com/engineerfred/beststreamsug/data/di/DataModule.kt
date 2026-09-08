package com.engineerfred.beststreamsug.data.di

import com.engineerfred.beststreamsug.core.network.NetworkErrorMapper
import com.engineerfred.beststreamsug.data.cache.ContentCatalogCache
import com.engineerfred.beststreamsug.data.remote.api.AppApiService
import com.engineerfred.beststreamsug.data.repository.BannerRepositoryImpl
import com.engineerfred.beststreamsug.data.repository.BrowseRepositoryImpl
import com.engineerfred.beststreamsug.data.repository.ContentDetailsRepositoryImpl
import com.engineerfred.beststreamsug.data.repository.EpisodeRepositoryImpl
import com.engineerfred.beststreamsug.data.repository.MetadataRepositoryImpl
import com.engineerfred.beststreamsug.data.repository.PlaybackProgressRepositoryImpl
import com.engineerfred.beststreamsug.data.repository.PlaybackRepositoryImpl
import com.engineerfred.beststreamsug.data.repository.RelatedContentRepositoryImpl
import com.engineerfred.beststreamsug.data.repository.SearchRepositoryImpl
import com.engineerfred.beststreamsug.data.repository.SectionRepositoryImpl
import com.engineerfred.beststreamsug.domain.repository.BannerRepository
import com.engineerfred.beststreamsug.domain.repository.BrowseRepository
import com.engineerfred.beststreamsug.domain.repository.ContentDetailsRepository
import com.engineerfred.beststreamsug.domain.repository.EpisodeRepository
import com.engineerfred.beststreamsug.domain.repository.PlaybackProgressRepository
import com.engineerfred.beststreamsug.domain.repository.RelatedContentRepository
import com.engineerfred.beststreamsug.domain.repository.PlaybackRepository
import com.engineerfred.beststreamsug.domain.repository.MetadataRepository
import com.engineerfred.beststreamsug.domain.repository.SearchRepository
import com.engineerfred.beststreamsug.domain.repository.SectionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideContentCatalogCache(): ContentCatalogCache = ContentCatalogCache()

    @Provides
    @Singleton
    fun provideNetworkErrorMapper(): NetworkErrorMapper = NetworkErrorMapper()

    @Provides
    @Singleton
    fun provideAppApiService(retrofit: Retrofit): AppApiService =
        retrofit.create(AppApiService::class.java)

    @Provides
    @Singleton
    fun provideMetadataRepository(
        implementation: MetadataRepositoryImpl,
    ): MetadataRepository = implementation

    @Provides
    @Singleton
    fun provideBrowseRepository(
        implementation: BrowseRepositoryImpl,
    ): BrowseRepository = implementation

    @Provides
    @Singleton
    fun provideBannerRepository(
        implementation: BannerRepositoryImpl,
    ): BannerRepository = implementation

    @Provides
    @Singleton
    fun provideContentDetailsRepository(
        implementation: ContentDetailsRepositoryImpl,
    ): ContentDetailsRepository = implementation

    @Provides
    @Singleton
    fun provideEpisodeRepository(
        implementation: EpisodeRepositoryImpl,
    ): EpisodeRepository = implementation

    @Provides
    @Singleton
    fun provideRelatedContentRepository(
        implementation: RelatedContentRepositoryImpl,
    ): RelatedContentRepository = implementation

    @Provides
    @Singleton
    fun providePlaybackRepository(
        implementation: PlaybackRepositoryImpl,
    ): PlaybackRepository = implementation

    @Provides
    @Singleton
    fun provideSearchRepository(
        implementation: SearchRepositoryImpl,
    ): SearchRepository = implementation

    @Provides
    @Singleton
    fun provideSectionRepository(
        implementation: SectionRepositoryImpl,
    ): SectionRepository = implementation

    @Provides
    @Singleton
    fun providePlaybackProgressRepository(
        implementation: PlaybackProgressRepositoryImpl,
    ): PlaybackProgressRepository = implementation
}
