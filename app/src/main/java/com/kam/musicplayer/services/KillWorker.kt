package com.kam.musicplayer.services

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker.Result.success
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class KillWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        if (MusicPlayerService.isServiceRunning) {
            MusicPlayerService.run { it.tryToDie() }
        }
        val nextRunTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15)
        val runTime = SimpleDateFormat("HH:mm:ss").format(nextRunTime)
        Log.i(MusicPlayerService.WORK_TAG, "Kill Checked, next run time $runTime")

        return success()
    }
}