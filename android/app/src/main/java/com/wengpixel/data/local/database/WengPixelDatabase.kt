package com.wengpixel.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [HistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WengPixelDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        const val DATABASE_NAME = "wengpixel_db"
    }
}
