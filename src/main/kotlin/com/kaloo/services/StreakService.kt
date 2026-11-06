package com.kaloo.services

import com.kaloo.models.CheckInRequest
import com.kaloo.models.UserStreak
import com.kaloo.repositories.StreakRepository

/** Lógica de la rama "Racha". */
object StreakService {

    fun getStreak(username: String): UserStreak =
        StreakRepository.get(username)

    fun checkIn(request: CheckInRequest): UserStreak {
        require(request.username.isNotBlank()) { "username requerido" }
        // TODO(GPS): validar latitude/longitude contra la ubicación del gimnasio
        return StreakRepository.checkIn(request.username.trim())
    }
}
