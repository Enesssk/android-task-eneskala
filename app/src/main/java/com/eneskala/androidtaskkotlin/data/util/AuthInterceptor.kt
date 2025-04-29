package com.eneskala.androidtaskkotlin.data.util

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val token = tokenManager.getToken()
        Log.d("AuthInterceptor", "Retrieved Token: $token")

        val newRequest = if (!token.isNullOrEmpty()){
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        return chain.proceed(request)
    }
}