package com.kaloo.routes

import com.kaloo.models.CheckInRequest
import com.kaloo.services.StreakService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.streakRoutes() {
    route("/api/streak") {
        // GET /api/streak/{username} → ver racha
        get("/{username}") {
            val username = call.parameters["username"].orEmpty()
            if (username.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "username requerido"))
                return@get
            }
            call.respond(StreakService.getStreak(username))
        }

        // POST /api/streak/check-in → registrar asistencia de hoy
        post("/check-in") {
            try {
                val req = call.receive<CheckInRequest>()
                call.respond(StreakService.checkIn(req))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
    }
}
