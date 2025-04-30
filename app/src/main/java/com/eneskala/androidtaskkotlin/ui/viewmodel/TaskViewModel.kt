package com.eneskala.androidtaskkotlin.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.eneskala.androidtaskkotlin.data.repository.TaskRepository
import com.eneskala.androidtaskkotlin.data.util.Resource
import com.eneskala.androidtaskkotlin.data.worker.TaskRefreshWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import androidx.lifecycle.switchMap
import com.eneskala.androidtaskkotlin.data.model.Task


@HiltViewModel
class TaskViewModel @Inject constructor(private val repo: TaskRepository,
    workManager: WorkManager
) : ViewModel() {


    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String>
        get() = _searchQuery

    // If nothing is written, I bring all tasks. If it is written, I search.
    val tasks = _searchQuery.switchMap { q ->
        if (q.isBlank()) {
            repo.getTasks()
        } else {
            repo.searchTasks(q)
        }
    }

    private val _refreshStatus = MutableLiveData<Resource<List<Task>>>()
    val refreshStatus: LiveData<Resource<List<Task>>>
        get() = _refreshStatus

    // My code block that provides the search.
    fun setQuery(q: String) {
        _searchQuery.value = q
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshStatus.value = Resource.loading(null)
            val result = repo.refreshTasks()
            // I pass the result to the ui layer.
            _refreshStatus.value = result
        }
    }

    init {
        // I call the worker as init to refresh every hour.
        val req = PeriodicWorkRequestBuilder<TaskRefreshWorker>(1, TimeUnit.HOURS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "refreshTasks",
            ExistingPeriodicWorkPolicy.KEEP, // I set it as unique. This way I don't create double work.
            req
        )
    }

}