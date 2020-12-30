package com.paigesoftware.workmanagertutorial

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.lang.Exception

class FilteringWorker(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {


    //Must Implement
    override fun doWork(): Result {
        try {
            for(i in 0 until 3000) {
                Log.i("MYTAG", "Filtering $i")
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

}