package com.eneskala.androidtaskkotlin.data.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class oAuth(
    val access_token: String
)


data class LoginResponse(
    val oauth: oAuth
)