package com.wengpixel.di

import com.wengpixel.data.repository.HistoryRepositoryImpl
import com.wengpixel.data.repository.ImageProcessingRepositoryImpl
import com.wengpixel.data.repository.SettingsRepositoryImpl
import com.wengpixel.domain.repository.HistoryRepository
import com.wengpixel.domain.repository.ImageProcessingRepository
import com.wengpixel.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindImageProcessingRepository(
        impl: ImageProcessingRepositoryImpl
    ): ImageProcessingRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        impl: HistoryRepositoryImpl
    ): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        impl: com.wengpixel.core.common.ConnectivityNetworkMonitor
    ): com.wengpixel.core.common.NetworkMonitor
}
