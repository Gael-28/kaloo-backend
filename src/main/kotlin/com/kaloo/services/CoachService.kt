package com.kaloo.services

import com.kaloo.models.ApiResponse
import com.kaloo.models.CoachMessageRequest

/** MensajerIa con entrenadores (placeholder: acA irA la BD / push). */
object CoachService {

    fun sendMessage(request: CoachMessageRequest): ApiResponse =
        ApiResponse(success = true, message = "Mensaje enviado exitosamente al entrenador '${request.coachName}'.")
}
