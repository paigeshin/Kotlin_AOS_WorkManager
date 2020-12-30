package com.paigesoftware.workmanagertutorial

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class DownloadingWorker(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {

    companion object {
        const val KEY_WORKER = "key_worker"
    }

    //Must Implement
    override fun doWork(): Result {
        try {

            for(i in 0 until 3000) {
                Log.i("MYTAG", "Uploading $i")
            }

            // Check if periodic work requests work well.
            val time = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
            val currentDate = time.format(Date())
            Log.i("MYTAG", "Completed $currentDate")

            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

}