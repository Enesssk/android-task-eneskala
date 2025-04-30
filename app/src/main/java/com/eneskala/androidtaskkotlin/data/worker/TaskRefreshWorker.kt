package com.eneskala.androidtaskkotlin.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eneskala.androidtaskkotlin.data.repository.TaskRepository
import com.eneskala.androidtaskkotlin.data.util.Status

class TaskRefreshWorker(
    ctx: Context,
    params: WorkerParameters,
    private val repo: TaskRepository
) : CoroutineWorker(ctx, params) { // I am running it asynchronously with threads.
    override suspend fun doWork(): Result = when( // I run my function every time the worker is triggered.
        repo.refreshTasks().status
    ) {
        Status.SUCCESS -> Result.success()
        else -> Result.retry()
    }
}