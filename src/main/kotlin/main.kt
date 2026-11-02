package com.kaloo

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val username: String? = null,
    val role: String? = null,
    val message: String? = null
)

@Serializable
data class User(val username: String, val password: String, val role: String)