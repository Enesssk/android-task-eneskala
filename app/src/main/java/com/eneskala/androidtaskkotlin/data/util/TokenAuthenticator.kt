package com.eneskala.androidtaskkotlin.data.util

import com.eneskala.androidtaskkotlin.data.model.LoginRequest
import com.eneskala.androidtaskkotlin.data.service.ApiService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

class TokenAuthenticator @Inject constructor(
    private val apiServiceProvider: Provider<ApiService>, // for code 401
    private val tokenManager: TokenManager
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        // If the previous attempt failed
        if (responseCount(response) > 1) return null

        // I am sending a synchronous login request.
        val loginResp = runBlocking {
            apiServiceProvider.get().login(LoginRequest("365","1"))
        }
        // If the login request is successful, I save the token.
        if (loginResp.isSuccessful) {
            val newToken = loginResp.body()!!.oauth.access_token
            tokenManager.saveToken(newToken)

            // I recreate the original request with the new token.
            return response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        }
        return null
    }

    //I'm preventing the endless code 401 loop.
    private fun responseCount(resp: Response): Int {
        var res = resp.priorResponse
        var result = 1
        while (res != null) {
            result++
            res = res.priorResponse
        }
        return result
    }
}