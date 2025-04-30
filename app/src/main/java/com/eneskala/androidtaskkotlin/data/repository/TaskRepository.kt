package com.eneskala.androidtaskkotlin.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.eneskala.androidtaskkotlin.data.local.dao.TaskDao
import com.eneskala.androidtaskkotlin.data.model.LoginRequest
import com.eneskala.androidtaskkotlin.data.model.LoginResponse
import com.eneskala.androidtaskkotlin.data.model.Task
import com.eneskala.androidtaskkotlin.data.service.ApiService
import com.eneskala.androidtaskkotlin.data.util.NetworkHelper
import com.eneskala.androidtaskkotlin.data.util.Resource
import com.eneskala.androidtaskkotlin.data.util.TokenManager
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject
import kotlin.math.log

class TaskRepository @Inject constructor(
    private val apiService: ApiService,
    private val dao: TaskDao,
    private val networkHelper: NetworkHelper
) {


    fun getTasks() = dao.getAll()
    fun searchTasks(q: String) = dao.search(q)

    // I retrieved the data from the API and saved it locally with room.
    suspend fun refreshTasks(): Resource<List<Task>> {

        // 1) control of offline
        if (!networkHelper.isNetworkAvailable()) {
            val cached = dao.getAllOnce()
            return Resource.success(cached)
        }

        return try {
        val response = apiService.getTasks()

            if(response.isSuccessful) {
                response.body()?.let { list ->
                    dao.insertAll(list) // I saved the data I captured locally
                    Resource.success(list)
                } ?: Resource.error("Error: ${response.errorBody()?.string()}", null)

            } else {
                val errorBody = response.errorBody()?.string()

                val errorMessage = if (!errorBody.isNullOrEmpty()) {
                    try {
                        val jsonObject = JSONObject(errorBody)
                        jsonObject.getString("message")
                    } catch (e: JSONException) {
                        "An error occurred: $errorBody"
                    }
                } else {
                    response.errorBody()?.string() ?: "unknown error"
                }

                Resource.error(errorMessage, null)
            }

        } catch (e: Exception) {
            Resource.error(e.message ?: "Fetch failed", null)
        }
    }



}
