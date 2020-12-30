package com.paigesoftware.workmanagertutorial

import android.content.Context
import android.util.Log
import androidx.work.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class UploadWorker(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {

    companion object {
        const val KEY_WORKER = "key_worker"
    }

    //Must Implement
    override fun doWork(): Result {
        try {
            val count = inputData.getInt(MainActivity.KEY_COUNT_VALUE, 0)
            for(i in 0 until count) {
                Log.i("MYTAG", "Uploading $i")
            }

            /** Sending data from Worker to activity **/
            val time = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
            val currentDate = time.format(Date())
            val outPutData = Data.Builder()
                .putString(KEY_WORKER, currentDate)
                .build()

            /** Sending data to activity **/
            //add argument to the result
            //you dont need to always pass argument
            return Result.success(outPutData)
        } catch (e: Exception) {
            return Result.failure()
        }
    }

}