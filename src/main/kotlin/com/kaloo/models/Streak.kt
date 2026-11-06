package com.kaloo.models

import kotlinx.serialization.Serializable

/** Rama "Racha": días consecutivos de asistencia/entrenamiento. */
@Serializable
data class UserStreak(
    val username: String,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCheckIns: Int = 0,
    val lastCheckIn: String? = null, // fecha ISO yyyy-MM-dd
    val message: String? = null
)

@Serializable
data class CheckInRequest(
    val username: String,
    // GPS opcional (para validar que está en el gimnasio)
    val latitude: Double? = null,
    val longitude: Double? = null
)
