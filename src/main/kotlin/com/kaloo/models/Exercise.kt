package com.kaloo.models

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseRequest(val goal: String)

@Serializable
data class ExerciseResponse(val goal: String, val routine: List<String>)
