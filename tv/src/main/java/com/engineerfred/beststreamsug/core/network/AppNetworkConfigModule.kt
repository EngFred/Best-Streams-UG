package com.engineerfred.beststreamsug.core.network

import com.engineerfred.beststreamsug.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppNetworkConfigModule {
    @Provides
    @Singleton
    fun provideNetworkConfig(): NetworkConfig = object : NetworkConfig {
        override val baseUrl: String = BuildConfig.API_BASE_URL
        override val isDebug: Boolean = BuildConfig.DEBUG
    }
}