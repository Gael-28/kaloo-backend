package com.kaloo.models

import kotlinx.serialization.Serializable

@Serializable
data class NutritionRequest(val weight: Float, val height: Float)

@Serializable
data class NutritionResponse(val bmi: Float, val recommendation: String)
