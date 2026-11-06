package com.kaloo.models

import kotlinx.serialization.Serializable

@Serializable
data class CoachMessageRequest(val username: String, val coachName: String, val message: String)

@Serializable
data class ApiResponse(val success: Boolean, val message: String)
