package com.paigesoftware.workmanagertutorial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.work.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        const val KEY_COUNT_VALUE = "key_count"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            //setOneTimeWorkRequest()
            setPeriodicWorkRequest()
        }

    }

    //*** Use WorkManager***
    private fun setOneTimeWorkRequest() {

        val workManager = WorkManager.getInstance(applicationContext)

        /** Worker IO, define data, Sending Data from activity to Worker **/
        val data: Data = Data.Builder()
            .putInt(KEY_COUNT_VALUE, 125)
            .build()

        /** set constraints **/
        val constraints = Constraints.Builder()
            .setRequiresCharging(true) //charging이 어느정도 필요할 때만 진행하게 한다.
            .setRequiredNetworkType(NetworkType.CONNECTED) //네트워크가 연결되었을 때만 나오게 한다.
            .build()

        /*** WorkManager Chaining ***/
        // 1. first Work Request
        val uploadRequest = OneTimeWorkRequest.Builder(UploadWorker::class.java)
            .setConstraints(constraints) /** set constraint **/
            .setInputData(data) /** Worker IO, Sending Data from activity to Worker **/
            .build()

        /*** WorkManager Chaining ***/
        // 2. Second Work Request
        val filteringRequest = OneTimeWorkRequest.Builder(FilteringWorker::class.java)
            .build()

        /*** WorkManager Chaining ***/
        // 3. Second Work Request
        val compressingRequest = OneTimeWorkRequest.Builder(CompressingWorker::class.java)
            .build()

        /*** WorkManager Chaining ***/
        // 4. Second Work Request
        val downloadingRequest = OneTimeWorkRequest.Builder(DownloadingWorker::class.java)
            .build()

        /*** WorkManager Chaining, Parallel Request ***/
        val parallelWorks: MutableList<OneTimeWorkRequest> = mutableListOf<OneTimeWorkRequest>()
        parallelWorks.add(downloadingRequest)
        parallelWorks.add(filteringRequest)

        /*** WorkManager Chaining, Sequential ***/
//        workManager
//            .beginWith(filteringRequest)
//            .then(compressingRequest)
//            .then(uploadRequest)
//            .enqueue()

        /*** WorkManager Chaining, Sequential ***/
        workManager
            .beginWith(parallelWorks)
            .then(compressingRequest)
            .then(uploadRequest)
            .enqueue()


//        workManager.enqueue(uploadRequest)

        //*** get work manager status ***
        workManager.getWorkInfoByIdLiveData(uploadRequest.id)
            .observe(this, Observer {
                textView.text = it.state.name //it returns blocked, enqueued, running, succeeded

                /*** Receive data from WorkManager ***/
                if(it.state.isFinished) {
                    val receivedData = it.outputData
                    val message = receivedData.getString(UploadWorker.KEY_WORKER)
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                }

            })

    }

    /*** PeriodicWorkRequest ***/
    private fun setPeriodicWorkRequest() {
        val periodicWorkRequest = PeriodicWorkRequest
            .Builder(DownloadingWorker::class.java, 16, TimeUnit.MINUTES) //1분씩 16번
            .build()
        WorkManager.getInstance(applicationContext).enqueue(periodicWorkRequest)
    }

}

