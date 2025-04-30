package com.eneskala.androidtaskkotlin.data.service

import com.eneskala.androidtaskkotlin.data.model.LoginRequest
import com.eneskala.androidtaskkotlin.data.model.LoginResponse
import com.eneskala.androidtaskkotlin.data.model.Task
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {


    @POST("index.php/login")
    suspend fun login(@Body login: LoginRequest): Response<LoginResponse>

    @GET("dev/index.php/v1/tasks/select")
    suspend fun getTasks(): Response<List<Task>>

}