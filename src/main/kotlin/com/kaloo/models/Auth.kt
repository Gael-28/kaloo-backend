package com.kaloo.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String? = null,
    val role: String? = null,
    val username: String? = null
)
