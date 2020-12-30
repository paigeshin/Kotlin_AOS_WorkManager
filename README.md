# Dependency - on app level

```kotlin
def work_version = "2.4.0"
implementation "androidx.work:work-runtime-ktx:$work_version"

```

ℹ️ WorkManager is intended for tasks that are **defferrable -** that is, not required to run immediately - and required to **run reliably** even if the app exits or the device restarts.

- Sending logs or analytics to backend services
- Periodically syncing application data with a server

### Concepts

- Chaining Tasks
- Status Updates
- Constraints
- Minimum resource usage
- Supports Different Versions
- Asynchronous Tasks
- Periodic Tasks
- One Time Work Request

ℹ️ Work Manager is not for tasks that need to be run in a background thread but don't need to survive process death.

⇒ for normal background task, we don't need `work manager`. 

⇒ We use `RxJava` or `Coroutine` for normal background task.  

### How to use

1. Create a Work class
2. Create a Work request
3. Enqueue the request
4. Get the status updates 

# WorkManager One Time Work Request Example

```kotlin
class UploadWorker(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {

    //Must Implement
    override fun doWork(): Result {
        try {
            for(i in 0..600) {
                Log.i("MYTAG", "Uploading $i")
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener { setOneTimeWorkRequest() }

    }

    //*** Use WorkManage r***
    private fun setOneTimeWorkRequest() {
        val uploadRequest = OneTimeWorkRequest.Builder(UploadWorker::class.java)
            .build()
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.enqueue(uploadRequest)

     

    }

}

```

# WorkManager Status

### WorkManger Status

- Blocked
- Enqueued
- Running
- Succeeded

```kotlin
//*** get work manager status ***
  workManager.getWorkInfoByIdLiveData(uploadRequest.id)
      .observe(this, Observer {
          textView.text = it.state.name //it returns blocked, enqueued, running, succeeded
      })
```

```kotlin
//*** Use WorkManager***
    private fun setOneTimeWorkRequest() {
        val uploadRequest = OneTimeWorkRequest.Builder(UploadWorker::class.java)
            .build()
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.enqueue(uploadRequest)

        //*** get work manager status ***
        workManager.getWorkInfoByIdLiveData(uploadRequest.id)
            .observe(this, Observer {
                textView.text = it.state.name //it returns blocked, enqueued, running, succeeded
            })

    }
```

# Constraint

- 어떤 특정 조건일 때만, workManager가 일을 할 수록 한다.

```kotlin
/** set constraints **/
val constraints = Constraints.Builder()
  .setRequiresCharging(true) //charging이 어느정도 필요할 때만 진행하게 한다.
  .setRequiredNetworkType(NetworkType.CONNECTED) //네트워크가 연결되었을 때만 나오게 한다.
  .build()
```

```kotlin

//*** Use WorkManager***
private fun setOneTimeWorkRequest() {

    /** set constraints **/
    val constraints = Constraints.Builder()
        .setRequiresCharging(true) //charging이 어느정도 필요할 때만 진행하게 한다.
        .setRequiredNetworkType(NetworkType.CONNECTED) //네트워크가 연결되었을 때만 나오게 한다.
        .build()

    val workManager = WorkManager.getInstance(applicationContext)

    val uploadRequest = OneTimeWorkRequest.Builder(UploadWorker::class.java)
        .setConstraints(constraints)
        .build()

    workManager.enqueue(uploadRequest)

    //*** get work manager status ***
    workManager.getWorkInfoByIdLiveData(uploadRequest.id)
        .observe(this, Observer {
            textView.text = it.state.name //it returns blocked, enqueued, running, succeeded
        })

}

```

# Set Input & Output Data of a worker class

- Activity to Worker ⇒ Input Data
- Worker to Activity ⇒ Output Data

### Activity → Worker

```kotlin
/** Worker IO, define data **/
val data: Data = Data.Builder()
    .putInt(KEY_COUNT_VALUE, 125)
    .build()

val uploadRequest = OneTimeWorkRequest.Builder(UploadWorker::class.java)
    .setInputData(data) /** Worker IO, set data **/
    .build()
```

```kotlin
private fun setOneTimeWorkRequest() {

        val workManager = WorkManager.getInstance(applicationContext)

        /** Worker IO, define data **/
        val data: Data = Data.Builder()
            .putInt(KEY_COUNT_VALUE, 125)
            .build()

        /** set constraints **/
        val constraints = Constraints.Builder()
            .setRequiresCharging(true) //charging이 어느정도 필요할 때만 진행하게 한다.
            .setRequiredNetworkType(NetworkType.CONNECTED) //네트워크가 연결되었을 때만 나오게 한다.
            .build()

        val uploadRequest = OneTimeWorkRequest.Builder(UploadWorker::class.java)
            .setConstraints(constraints) /** set constraint **/
            .setInputData(data) /** Worker IO, set data **/
            .build()

        workManager.enqueue(uploadRequest)

        //*** get work manager status ***
        workManager.getWorkInfoByIdLiveData(uploadRequest.id)
            .observe(this, Observer {
                textView.text = it.state.name //it returns blocked, enqueued, running, succeeded
            })

    }
```

```kotlin
class UploadWorker(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {

    //Must Implement
    override fun doWork(): Result {
        try {
						/** Worker IO, Get input data **/
            val count = inputData.getInt(MainActivity.KEY_COUNT_VALUE, 0)
            for(i in 0 until count) {
                Log.i("MYTAG", "Uploading $i")
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

}
```

### Work → Activity

```kotlin
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
```

```kotlin
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

        val uploadRequest = OneTimeWorkRequest.Builder(UploadWorker::class.java)
            .setConstraints(constraints) /** set constraint **/
            .setInputData(data) /** Worker IO, Sending Data from activity to Worker **/
            .build()

        workManager.enqueue(uploadRequest)

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
```

# Chaining Workers

- Workers

```kotlin
class CompressingWorker(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {

    //Must Implement
    override fun doWork(): Result {
        try {
            for(i in 0 until 300) {
                Log.i("MYTAG", "Compressing $i")
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

}

class FilteringWorker(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {

    //Must Implement
    override fun doWork(): Result {
        try {
            for(i in 0 until 300) {
                Log.i("MYTAG", "Filtering $i")
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

}

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
```

- Sequential Chaining

```kotlin
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
workManager
    .beginWith(filteringRequest)
    .then(compressingRequest)
    .then(uploadRequest)
    .enqueue()
```

```kotlin
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
      workManager
          .beginWith(filteringRequest)
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
```

- Parallel Request

```kotlin
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
```

- MainActivity Entire Code

```kotlin
package com.paigesoftware.workmanagertutorial

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.work.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val KEY_COUNT_VALUE = "key_count"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener { setOneTimeWorkRequest() }

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

}
```

# Periodic Work Requests

```kotlin
private fun setPeriodicWorkRequest() {
        val periodicWorkRequest = PeriodicWorkRequest
            .Builder(DownloadingWorker::class.java, 16, TimeUnit.MINUTES) //1분씩 16번
            .build()
        WorkManager.getInstance(applicationContext).enqueue(periodicWorkRequest)
    }
```

```kotlin
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
```

- Entire code

```kotlin
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
```

```kotlin
class CompressingWorker(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {

    //Must Implement
    override fun doWork(): Result {
        try {
            for(i in 0 until 300) {
                Log.i("MYTAG", "Compressing $i")
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.failure()
        }
    }

}

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
```