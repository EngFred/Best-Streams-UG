package com.engineerfred.beststreamsug.mobile

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.engineerfred.beststreamsug.mobile.core.notification.NewContentNotificationWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class BestStreamsMobileApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        scheduleNewContentWorker()
    }

    private fun scheduleNewContentWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 30-minute interval for timely new release detection
        val periodicWorkRequest = PeriodicWorkRequestBuilder<NewContentNotificationWorker>(
            repeatInterval = 30,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
        )
            .setConstraints(constraints)
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<NewContentNotificationWorker>()
            .setConstraints(constraints)
            .build()

        val workManager = WorkManager.getInstance(this)
        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest,
        )
        workManager.enqueueUniqueWork(
            INITIAL_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            oneTimeWorkRequest,
        )
        Log.i(TAG, "Enqueued 30-minute periodic worker and immediate one-time check")
    }

    companion object {
        private const val TAG = "BestStreamsApp"
        private const val WORK_NAME = "BestStreamsNewContentWorker"
        private const val INITIAL_WORK_NAME = "BestStreamsInitialNewContentWorker"
    }
}