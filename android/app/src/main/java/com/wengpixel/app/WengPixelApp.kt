package com.wengpixel.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.wengpixel.worker.ImageCleanupWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class WengPixelApp : Application() {

    override fun onCreate() {
        super.onCreate()
        setupBackgroundCleanup()
    }

    private fun setupBackgroundCleanup() {
        val cleanupRequest = PeriodicWorkRequestBuilder<ImageCleanupWorker>(24, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "WengPixelImageCleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )
    }
}
