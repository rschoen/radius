package com.ryanschoen.radius

import android.app.Application
import androidx.work.*
import com.ryanschoen.radius.work.RefreshNetworkDataWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

class RadiusApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        delayedInit()
    }

    private fun delayedInit() {
        applicationScope.launch {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                .setRequiresDeviceIdle(true)
                .build()

            val repeatingRequest = PeriodicWorkRequestBuilder<RefreshNetworkDataWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                RefreshNetworkDataWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                repeatingRequest
            )

        }
    }
}