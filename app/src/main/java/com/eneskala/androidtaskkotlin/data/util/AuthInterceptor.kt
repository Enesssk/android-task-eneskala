package com.eneskala.androidtaskkotlin.data.util

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import okhttp3.Request
import okio.Buffer
import java.nio.charset.Charset


class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        logCurlCommand(chain.request())

        // 1) I added auth header for each login
        if (original.url.encodedPath.endsWith("/index.php/login")) {
            builder
                .addHeader(
                    "Authorization",
                    "Basic QVBJX0V4cGxvcmVyOjEyMzQ1NmlzQUxhbWVQYXNz"
                )
                .addHeader("Content-Type", "application/json")
        } else {
            // I added tokens for all other requests
            tokenManager.getToken()?.let { token ->
                builder.addHeader("Authorization", "Bearer $token")
            }
        }

        val newRequest = builder.build()
        Log.d("AuthInterceptor", "Request to ${newRequest.url} headers=${newRequest.headers}")
        logCurlCommand(newRequest)
        return chain.proceed(newRequest)
    }

    //for curl
    private fun logCurlCommand(request: Request) {
        val curlCommand = StringBuilder("curl -X ${request.method} \"${request.url}\"")

        request.headers.names().forEach { name ->
            curlCommand.append(" -H \"$name: ${request.header(name)}\"")
        }

        request.body?.let { body ->
            val buffer = Buffer()
            body.writeTo(buffer)
            val charset = body.contentType()?.charset(Charset.forName("UTF-8")) ?: Charset.forName("UTF-8")
            val requestBody = buffer.readString(charset)

            if (requestBody.isNotEmpty()) {
                curlCommand.append(" --data '${requestBody}'")
            }
        }

        Log.d("CURL_REQUEST", curlCommand.toString())
    }

}
