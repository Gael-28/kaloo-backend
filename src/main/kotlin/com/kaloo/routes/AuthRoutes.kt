package com.kaloo.routes

import com.kaloo.models.LoginRequest
import com.kaloo.services.AuthService
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    post("/login") {
        val req = call.receive<LoginRequest>()
        call.respond(AuthService.login(req))
    }
}
