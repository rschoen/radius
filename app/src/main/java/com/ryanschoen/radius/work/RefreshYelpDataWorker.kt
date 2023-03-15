package com.ryanschoen.radius.work

import android.app.Application
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ryanschoen.radius.repository.VenuesRepository

class RefreshYelpDataWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    companion object {
        const val WORK_NAME = "RefreshYelpDataWorker"
    }
    override suspend fun doWork(): Result {
        val repo = VenuesRepository(applicationContext as Application)

        return try {
            val address = repo.getSavedAddress()
            if(!address.isNullOrEmpty()) {
                repo.clearYelpData()
                repo.downloadVenues(address)
                Result.success()
            }else {
                Result.failure()
            }
        } catch (e: Throwable) {
            Result.retry()
        }
    }
}