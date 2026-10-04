package com.wengpixel.di

import android.content.Context
import androidx.room.Room
import com.wengpixel.data.local.database.HistoryDao
import com.wengpixel.data.local.database.WengPixelDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WengPixelDatabase {
        return Room.databaseBuilder(
            context,
            WengPixelDatabase::class.java,
            WengPixelDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideHistoryDao(database: WengPixelDatabase): HistoryDao {
        return database.historyDao()
    }
}
