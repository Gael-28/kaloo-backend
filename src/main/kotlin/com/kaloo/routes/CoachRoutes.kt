package com.kaloo.routes

import com.kaloo.models.CoachMessageRequest
import com.kaloo.services.CoachService
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.coachRoutes() {
    post("/api/coaches/message") {
        val req = call.receive<CoachMessageRequest>()
        call.respond(CoachService.sendMessage(req))
    }
}
