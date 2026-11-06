package com.kaloo.services

import com.kaloo.models.ApiResponse
import com.kaloo.models.CoachMessageRequest

/** Mensajería con entrenadores (placeholder: acá irá la BD / push). */
object CoachService {

    fun sendMessage(request: CoachMessageRequest): ApiResponse =
        ApiResponse(success = true, message = "Mensaje enviado exitosamente al entrenador '${request.coachName}'.")
}
